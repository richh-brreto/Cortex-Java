package school.sptech.cortex.monitoramento.app;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import school.sptech.cortex.monitoramento.dao.LimiteDAO;
import school.sptech.cortex.monitoramento.dao.processoDAO;
import school.sptech.cortex.monitoramento.modelo.*;
import school.sptech.cortex.monitoramento.service.ProcessadorDeCapturasService;
import school.sptech.cortex.monitoramento.service.ProcessadorDeProcessos;
import school.sptech.cortex.monitoramento.util.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Aplicacao implements RequestHandler<S3Event, String> {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private static final String DESTINATION_BUCKET = "cortex-trusted-s3-bck";
    private static final String SOURCE = "cortex-raw-s3-bck";
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");


    @Override
    public String handleRequest(S3Event s3Event, Context context) {

        LambdaLogger logger = context.getLogger();

        logger.log("=================================================");
        logger.log("   CORTEX - INICIANDO PROCESSO DE MONITORAMENTO  ");
        logger.log("=================================================");


        String sourceBucket = s3Event.getRecords().get(0).getS3().getBucket().getName();
        String sourceKey = s3Event.getRecords().get(0).getS3().getObject().getKey();

        // 1. LEITURA E CARREGAMENTO DOS DADOS DO CSV
        logger.log("\n[1] Lendo capturas do arquivo:" + sourceKey);


        try {
            InputStream s3InputStream = s3Client.getObject(sourceBucket, sourceKey).getObjectContent();

            CsvReader reader = new CsvReader();
            List<CapturaSistema> capturas = reader.lerECarregarCapturasSistema(s3InputStream);

            // AQUI VAI COMEÇAR O TRATAMENTO DE ALERTAS
            checarAlerta(capturas, sourceKey, logger);


            logger.log("-> Exportando capturas para o arquivo de saída:" + sourceKey);
            CsvWriter csvWriter = new CsvWriter();
            ByteArrayOutputStream csvOutputStream = csvWriter.writeCsv(capturas);

            InputStream csvInputStream = new ByteArrayInputStream(csvOutputStream.toByteArray());
            s3Client.putObject(DESTINATION_BUCKET, sourceKey, csvInputStream, null);

            s3InputStream.close();
            csvInputStream.close();
            csvOutputStream.close();
            return "Processamento concluído;";
        }catch (Exception e) {
            context.getLogger().log("Erro: " + e.getMessage());
            logger.log(Arrays.toString(e.getStackTrace()));
            return "Erro no processamento";
        }

    }


    public void checarAlerta(List<CapturaSistema> capturas, String sourceKey, LambdaLogger logger)  throws Exception{
        if (capturas == null || capturas.isEmpty()) {
            logger.log("Erro: Capturas está vazio");
            throw new IllegalStateException("Lista de capturas vazia");
        }

        ProcessadorDeCapturasService processador = new ProcessadorDeCapturasService();
        String[] splitTitulo2 = sourceKey.split("/");
        String[] splitTitulo = splitTitulo2[1].split(";");

        String timestampTituloStr = splitTitulo[0];
        LocalDateTime timestampTitulo = LocalDateTime.parse(timestampTituloStr, FORMATADOR_TIMESTAMP);


        LimiteDAO limiteComando = new LimiteDAO();
        Parametro limite = limiteComando.buscarLimitesPorMaquina(capturas.get(0).getFk_modelo());

        if (limite == null) {
            logger.log("Erro: Não foi possível achar limites da máquina");
            throw new IllegalStateException("Limites da máquina não encontrados para: " + capturas.get(0).getFk_modelo());
        }

        String fk_modelo = capturas.get(0).getFk_modelo();
        String fk_zona = capturas.get(0).getFk_zona();
        String fk_empresa = capturas.get(0).getFk_empresa();


        EstadoAtualChecar estado = new EstadoAtualChecar();
        String jsonEstadoAtual = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + "estadoAtual.json";
        EstadoAtual estadoAlerta = estado.checarEstadoAtual(jsonEstadoAtual, s3Client, DESTINATION_BUCKET);

        if (estadoAlerta == null) {
            logger.log("Erro: Estado Atual não encontrado");
            throw new IllegalStateException("Estado Atual não encontrado: " + jsonEstadoAtual);
        }

        if((estadoAlerta.getGpu() || estadoAlerta.getCpu() || estadoAlerta.getRam() ||
                estadoAlerta.getDisco()) && estadoAlerta.getTimestamp() != null){
            if(estadoAlerta.getTimestamp().plusMinutes(6).isAfter(timestampTitulo)){
                // DOWNTIME
                // csv Historico

                Duration duracao = Duration.between(timestampTitulo,estadoAlerta.getUltimoTimestamp());

                long minutos = duracao.toMinutes();
                List<HistoricoAlerta> historicoDowntime = new ArrayList<>();
                for(int i = 0; i < minutos; i++){

                    LocalDateTime timestampDown = timestampTitulo.plusMinutes(i);
                    HistoricoAlerta h = new HistoricoAlerta(false,false,false,false,0.0,
                            0.0,0.0,0.0,
                            timestampDown, true, true,0.0,
                            0.0,0.0);

                    historicoDowntime.add(h);
                }

                salvarCsvHistorico(fk_modelo,  fk_zona,
                         fk_empresa,  estadoAlerta.getIdJira(),historicoDowntime);
                // limpar json
                estadoAlerta.setCpu(false);
                estadoAlerta.setRam(false);
                estadoAlerta.setGpu(false);
                estadoAlerta.setDisco(false);
                estadoAlerta.setTimestamp(null);
                estadoAlerta.setIdJira(null);

            }
        }
        estadoAlerta.setUltimoTimestamp(timestampTitulo);
        CsvHistoricoReader leitor = new CsvHistoricoReader();

        String nome_arquivo_historico = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + estadoAlerta.getIdJira() + ";" + "historico.csv";
        // Lembrar que se não existir vai retornar como null
        List<HistoricoAlerta> historico = leitor.leExibeArquivoCsv(nome_arquivo_historico,s3Client,DESTINATION_BUCKET);




        SlackNotifier slack = new SlackNotifier();
        JiraTicketCreator jira = new JiraTicketCreator();
        JiraConcatenar concatenar = new JiraConcatenar();

        Alerta cpu = null;
        Alerta gpu = null;
        Alerta ram = null;
        Alerta disco = null;

        // PERCORRER PROCESSOS

        processoDAO processoComando = new processoDAO();
        String nomeProcesso = processoComando.buscarNomeProcessoPrincipal(fk_modelo);

        String arquivoProcesso = "Processos;" + splitTitulo2;

        ProcessadorDeProcessos processadorProcesso = new ProcessadorDeProcessos();
        List<CapturaProcessoPrincipal> histProcessoPrncipal = processadorProcesso.historicoProcesso(s3Client,arquivoProcesso,SOURCE,nomeProcesso);
        for(int i = 0; i < capturas.size();i++){

            CapturaSistema c = capturas.get(i);
            CapturaProcessoPrincipal p = histProcessoPrncipal.get(i);
            // CPU
            if(estadoAlerta != null) {
                if (estadoAlerta.getCpu()) {
                    if (c.getCpu() > limite.getLimiteCpu()) {
                        estadoAlerta.setTimestamp(c.getTimestamp());
                    } else {
                        estadoAlerta.setCpu(false);
                        cpu = processador.checarEGerarAlerta("Cpu", c.getCpu(), limite.getLimiteCpu(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                    }
                } else {
                    cpu = processador.checarEGerarAlerta("Cpu", c.getCpu(), limite.getLimiteCpu(),
                            limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                            c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                }

                if (cpu != null) {

                    slack.enviarAlertas(cpu);
                    if (cpu.getTipo().equalsIgnoreCase("Critico")) {
                        if (estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()) {
                            estadoAlerta.setCpu(true);
                            estadoAlerta.setTimestamp(c.getTimestamp());
                            // CONCATENAR
                            concatenar.concatenarTicketsCriticos(cpu, estadoAlerta.getIdJira());
                        } else {
                            if (estadoAlerta.getTimestamp() == null || estadoAlerta.getTimestamp().plusMinutes(3).isAfter(c.getTimestamp())) {
                                // Configura novo ticket
                                // Criar novo Ticket
                                // retorna o id do ticket do jira
                                String idJira = jira.criarTicketsCriticos(cpu);
                                // atualiza json com novo estado - atualizar o objeto
                                estadoAlerta = new EstadoAtual(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, c.getTimestamp(), idJira, timestampTitulo);

                                // Cria novo csv de histórico
                                salvarCsvHistorico(fk_modelo, fk_zona, fk_empresa, estadoAlerta.getIdJira(), historico);

                                String arquivoHistorico = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + estadoAlerta.getIdJira() + "historico.csv";
                                historico.clear();
                                historico = leitor.leExibeArquivoCsv(arquivoHistorico, s3Client, DESTINATION_BUCKET);
                            } else {
                                // Não configura
                                // CONCATENAR
                                concatenar.concatenarTicketsCriticos(cpu, estadoAlerta.getIdJira());
                            }

                        }
                    }

                }

                // GPU
                if(estadoAlerta.getGpu()){
                    if(c.getGpu() > limite.getLimiteGpu()){
                        estadoAlerta.setTimestamp(c.getTimestamp());
                    }else {
                        estadoAlerta.setGpu(false);
                        gpu = processador.checarEGerarAlerta("Gpu", c.getGpu(), limite.getLimiteGpu(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                    }
                }else {
                    gpu =  processador.checarEGerarAlerta("Gpu", c.getGpu(), limite.getLimiteGpu(),
                            limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                            c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                }

                if(gpu != null) {

                    slack.enviarAlertas(gpu);
                    if (gpu.getTipo().equalsIgnoreCase("Critico")) {
                        if (estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()) {
                            estadoAlerta.setGpu(true);
                            estadoAlerta.setTimestamp(c.getTimestamp());
                            // CONCATENAR
                            concatenar.concatenarTicketsCriticos(gpu, estadoAlerta.getIdJira());
                        } else {
                            if (estadoAlerta.getTimestamp() == null || estadoAlerta.getTimestamp().plusMinutes(3).isAfter(c.getTimestamp())) {
                                // Configura novo ticket
                                // Criar novo Ticket
                                // retorna o id do ticket do jira
                                String idJira = jira.criarTicketsCriticos(gpu);
                                // atualiza json com novo estado - atualizar o objeto
                                estadoAlerta = new EstadoAtual(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, c.getTimestamp(), idJira, timestampTitulo);

                                // Cria novo csv de histórico
                                salvarCsvHistorico(fk_modelo, fk_zona, fk_empresa, estadoAlerta.getIdJira(), historico);

                                String arquivoHistorico = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + estadoAlerta.getIdJira() + "historico.csv";
                                historico.clear();
                                historico = leitor.leExibeArquivoCsv(arquivoHistorico, s3Client, DESTINATION_BUCKET);
                            } else {
                                // Não configura
                                // CONCATENAR
                                concatenar.concatenarTicketsCriticos(gpu, estadoAlerta.getIdJira());
                            }

                        }
                    }
                }

                // RAM

                if(estadoAlerta.getRam()){
                    if(c.getRam() > limite.getLimiteRam()){
                        estadoAlerta.setTimestamp(c.getTimestamp());
                    }else {
                        estadoAlerta.setRam(false);
                        ram = processador.checarEGerarAlerta("Ram", c.getRam(), limite.getLimiteRam(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                    }
                }else {
                    ram =  processador.checarEGerarAlerta("Ram", c.getRam(), limite.getLimiteRam(),
                            limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                            c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                }

                if(ram != null) {

                    slack.enviarAlertas(ram);
                    if (ram.getTipo().equalsIgnoreCase("Critico")) {
                        if (estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()) {
                            estadoAlerta.setRam(true);
                            estadoAlerta.setTimestamp(c.getTimestamp());
                            // CONCATENAR
                            concatenar.concatenarTicketsCriticos(ram, estadoAlerta.getIdJira());
                        } else {
                            if (estadoAlerta.getTimestamp() == null || estadoAlerta.getTimestamp().plusMinutes(3).isAfter(c.getTimestamp())) {
                                // Configura novo ticket
                                // Criar novo Ticket
                                // retorna o id do ticket do jira
                                String idJira = jira.criarTicketsCriticos(ram);
                                // atualiza json com novo estado - atualizar o objeto
                                estadoAlerta = new EstadoAtual(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, c.getTimestamp(), idJira, timestampTitulo);

                                // Cria novo csv de histórico
                                salvarCsvHistorico(fk_modelo, fk_zona, fk_empresa, estadoAlerta.getIdJira(), historico);

                                String arquivoHistorico = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + estadoAlerta.getIdJira() + "historico.csv";
                                historico.clear();
                                historico = leitor.leExibeArquivoCsv(arquivoHistorico, s3Client, DESTINATION_BUCKET);
                            } else {
                                // Não configura
                                // CONCATENAR
                                concatenar.concatenarTicketsCriticos(ram, estadoAlerta.getIdJira());
                            }

                        }
                    }
                }

                // Disco

                if(estadoAlerta.getDisco()){
                    if(c.getDiscoUso() > limite.getLimiteDiscoUso()){
                        estadoAlerta.setTimestamp(c.getTimestamp());
                    }else {
                        estadoAlerta.setDisco(false);
                        disco = processador.checarEGerarAlerta("Disco", c.getDiscoUso(), limite.getLimiteDiscoUso(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                    }
                }else {
                    disco =  processador.checarEGerarAlerta("Disco", c.getDiscoUso(), limite.getLimiteDiscoUso(),
                            limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                            c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                }

                if(disco != null) {

                    slack.enviarAlertas(disco);
                    if (disco.getTipo().equalsIgnoreCase("Critico")) {
                        if (estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()) {
                            estadoAlerta.setDisco(true);
                            estadoAlerta.setTimestamp(c.getTimestamp());
                            // CONCATENAR
                            concatenar.concatenarTicketsCriticos(disco, estadoAlerta.getIdJira());
                        } else {
                            if (estadoAlerta.getTimestamp() == null || estadoAlerta.getTimestamp().plusMinutes(3).isAfter(c.getTimestamp())) {
                                // Configura novo ticket
                                // Criar novo Ticket
                                // retorna o id do ticket do jira
                                String idJira = jira.criarTicketsCriticos(disco);
                                // atualiza json com novo estado - atualizar o objeto
                                estadoAlerta = new EstadoAtual(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, c.getTimestamp(), idJira, timestampTitulo);

                                // Cria novo csv de histórico
                                salvarCsvHistorico(fk_modelo, fk_zona, fk_empresa, estadoAlerta.getIdJira(), historico);

                                String arquivoHistorico = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + estadoAlerta.getIdJira() + "historico.csv";
                                historico.clear();
                                historico = leitor.leExibeArquivoCsv(arquivoHistorico, s3Client, DESTINATION_BUCKET);
                            } else {
                                // Não configura
                                // CONCATENAR
                                concatenar.concatenarTicketsCriticos(disco, estadoAlerta.getIdJira());
                            }

                        }
                    }
                }
            }

            if(estadoAlerta.getRam() || estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getGpu()){

                // Atualizar csv de histórico atual usando o id, booleans do JSON E valores e timestamp da captura
                HistoricoAlerta novoHistorico = new HistoricoAlerta(estadoAlerta.getCpu(), estadoAlerta.getRam(),
                        estadoAlerta.getGpu(), estadoAlerta.getDisco(),c.getCpu(), c.getGpu(),c.getDiscoUso(),c.getRam(),
                        c.getTimestamp(),false,p.getDowntime(),p.getCpu(),p.getRam(),p.getGpu());

                historico.add(novoHistorico);
            }else{


               if(!historico.isEmpty() &&  historico.get(historico.size() - 1).getTimestamp().plusMinutes(3).isBefore(c.getTimestamp())){
                   HistoricoAlerta novoHistorico = new HistoricoAlerta(estadoAlerta.getCpu(), estadoAlerta.getRam(),
                           estadoAlerta.getGpu(), estadoAlerta.getDisco(),c.getCpu(), c.getGpu(),c.getDiscoUso(),c.getRam(),
                           c.getTimestamp(),false,p.getDowntime(),p.getCpu(),p.getRam(),p.getGpu());

                   historico.add(novoHistorico);

                }else{
                   // limpar JSON
                   estadoAlerta.setCpu(false);
                   estadoAlerta.setRam(false);
                   estadoAlerta.setGpu(false);
                   estadoAlerta.setDisco(false);
                   estadoAlerta.setTimestamp(null);
                   estadoAlerta.setIdJira(null);
               }

            }


        }
        // ESCREVER MEU CSV
            salvarCsvHistorico(fk_modelo,fk_zona,fk_empresa,estadoAlerta.getIdJira(),historico);
        // ATUALIZAR MEU JSON E MANDAR PRO BUCKET
        EstadoAtualWriter writerJson = new EstadoAtualWriter();
        writerJson.escreverEstadoAtual(jsonEstadoAtual,estadoAlerta,DESTINATION_BUCKET,s3Client);
    }

    public void salvarCsvHistorico(String fk_modelo, String fk_zona,
            String fk_empresa, String idJira, List<HistoricoAlerta> historico){


        CsvHistoricoWriter writer = new CsvHistoricoWriter();
        String nome_arquivo_historico = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + idJira + ";" + "historico.csv";
        writer.escreverCsv(DESTINATION_BUCKET,s3Client,historico,nome_arquivo_historico);

    }

}
