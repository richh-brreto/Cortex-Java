package school.sptech.cortex.monitoramento.app;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Aplicacao implements RequestHandler<S3Event, String> {

    private static final Log log = LogFactory.getLog(Aplicacao.class);
    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private static final String DESTINATION_BUCKET = "cortex-trusted-s3-bck";
    private static final String SOURCE = "cortex-raw-s3-bck";
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    LimiteDAO limiteComando = new LimiteDAO();
    SlackNotifier slack = new SlackNotifier();
    JiraTicketCreator jira = new JiraTicketCreator();
    JiraConcatenar concatenar = new JiraConcatenar();


    @Override
    public String handleRequest(S3Event s3Event, Context context) {

        LambdaLogger logger = context.getLogger();

        logger.log("=================================================");
        logger.log("   CORTEX - INICIANDO PROCESSO DE MONITORAMENTO  ");
        logger.log("=================================================");


        String sourceBucket = s3Event.getRecords().get(0).getS3().getBucket().getName();
        String sourceKey = s3Event.getRecords().get(0).getS3().getObject().getKey();

        logger.log("KEy:" + sourceKey);

        // Decodificar titulo
        String chaveDecodificadora = null;
        try {
            chaveDecodificadora = URLDecoder.decode(sourceKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        logger.log("Chave:" + chaveDecodificadora);

        try {

            logger.log("Lendo Capturas");
            InputStream s3InputStream = s3Client.getObject(sourceBucket, chaveDecodificadora).getObjectContent();

            CsvReader reader = new CsvReader();
            List<CapturaSistema> capturas = reader.lerECarregarCapturasSistema(s3InputStream);


            if (capturas == null || capturas.isEmpty()) {
                logger.log("Erro: Capturas está vazio");
                throw new IllegalStateException("Lista de capturas vazia");
            }





            String[] splitTitulo2 = chaveDecodificadora.split("/");
            String[] splitTitulo = splitTitulo2[1].split(";");
            String timestampTituloStr = splitTitulo[0];
            LocalDateTime timestampTitulo = LocalDateTime.parse(timestampTituloStr, FORMATADOR_TIMESTAMP);

            logger.log("strTimestamp Titulo:"+timestampTituloStr);
            logger.log("Timestamp Titulo:"+timestampTitulo);

            logger.log("buscando limites");

            Parametro limite = limiteComando.buscarLimitesPorMaquina(capturas.get(0).getFk_modelo());

            if (limite == null) {
                logger.log("Erro: Não foi possível achar limites da máquina");
                throw new IllegalStateException("Limites da máquina não encontrados para: " + capturas.get(0).getFk_modelo());
            }

            String fk_modelo = capturas.get(0).getFk_modelo();
            String fk_zona = capturas.get(0).getFk_zona();
            String fk_empresa = capturas.get(0).getFk_empresa();
            logger.log("FkModelo:" + fk_modelo);
            logger.log("FkZona:"+fk_zona);
            logger.log("FkEmpresa:" + fk_empresa);

            EstadoAtualChecar estado = new EstadoAtualChecar();
            String jsonEstadoAtual = fk_modelo + "-" + fk_zona + "-" + fk_empresa + "-" + "estadoAtual.json";
            EstadoAtual estadoAlerta = estado.checarEstadoAtual(jsonEstadoAtual, s3Client, DESTINATION_BUCKET, logger);
            LocalDateTime timestampEstado = null;

            if(estadoAlerta.getTimestamp() != null){
                timestampEstado = LocalDateTime.parse(estadoAlerta.getTimestamp(), FORMATADOR_TIMESTAMP);
            }








            List<HistoricoAlerta> historico = new ArrayList<>();

            if(estadoAlerta.getIdJira() != null){
                String nome_arquivo_historico = fk_modelo + "-" + fk_zona + "-" + fk_empresa + "-" + estadoAlerta.getIdJira() +".csv";
                historico = lerHistorico(nome_arquivo_historico,logger);
            }




            Alerta cpu = null;
            Alerta gpu = null;
            Alerta ram = null;
            Alerta disco = null;

            if((estadoAlerta.getGpu() || estadoAlerta.getCpu() || estadoAlerta.getRam() ||
                    estadoAlerta.getDisco()) && (estadoAlerta.getTimestamp() != null) &&
                    (estadoAlerta.getUltimoTimestamp() != null)){
                logger.log("DOWNTIME SERVER");

                LocalDateTime timestampUltimo = LocalDateTime.parse(estadoAlerta.getUltimoTimestamp(), FORMATADOR_TIMESTAMP);
                if(timestampEstado.plusMinutes(8).isBefore(timestampTitulo)){
                    // DOWNTIME
                    // csv Historico

                    Duration duracao = Duration.between(timestampUltimo, timestampTitulo);
                    long minutos = duracao.toMinutes();

                    List<HistoricoAlerta> historicoDowntime = new ArrayList<>();
                    for(int i = 0; i < minutos; i++){

                        LocalDateTime timestampDown = timestampUltimo.plusMinutes(i);
                        HistoricoAlerta h = new HistoricoAlerta(false,false,false,false,0.0,
                                0.0,0.0,0.0,
                                timestampDown, true, true,0.0,
                                0.0,0.0);

                        historico.add(h);
                    }

                    salvarCsvHistorico(fk_modelo,  fk_zona,
                            fk_empresa,  estadoAlerta.getIdJira(),historico, logger);
                    // limpar json
                    estadoAlerta.setCpu(false);
                    estadoAlerta.setRam(false);
                    estadoAlerta.setGpu(false);
                    estadoAlerta.setDisco(false);
                    estadoAlerta.setTimestamp(null);
                    estadoAlerta.setIdJira(null);

                }
            }

            estadoAlerta.setUltimoTimestamp(timestampTituloStr);
            // PERCORRER PROCESSOS

            processoDAO processoComando = new processoDAO();
            String nomeProcesso = processoComando.buscarNomeProcessoPrincipal(fk_modelo);

            String arquivoProcesso = "Processos;" + splitTitulo2[1];

            ProcessadorDeProcessos processadorProcesso = new ProcessadorDeProcessos();
            List<CapturaProcessoPrincipal> histProcessoPrncipal = processadorProcesso.historicoProcesso(s3Client,arquivoProcesso,SOURCE,nomeProcesso, logger, DESTINATION_BUCKET);
            logger.log("começando processamento de alertas");


            ProcessadorDeCapturasService processador = new ProcessadorDeCapturasService();
            for(int i = 0; i < capturas.size();i++){

                CapturaSistema c = capturas.get(i);
                logger.log("Da vez:"+ c.toString());
               CapturaProcessoPrincipal p = histProcessoPrncipal.get(i);
                String capturaTimestamp = c.getTimestamp().format(FORMATADOR_TIMESTAMP);

                logger.log("CPU");
                // CPU
                if(estadoAlerta != null) {
                    logger.log("Estado cpu:" + estadoAlerta.getCpu());
                    if (estadoAlerta.getCpu()) {
                        logger.log("Cpu = true");

                        if (c.getCpu() > limite.getLimiteCpu()) {

                            logger.log("Ultrapassou o limite (cpu)");

                            estadoAlerta.setTimestamp(capturaTimestamp);

                            logger.log("Estado atual timestamp:"+estadoAlerta.getTimestamp());
                            cpu = null;
                        } else {
                            logger.log("Cpu n ultrapassa o limite");
                            estadoAlerta.setCpu(false);

                            logger.log("Checando alerta (cpu");
                            cpu = processador.checarEGerarAlerta("Cpu", c.getCpu(), limite.getLimiteCpu(),
                                    limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                    c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome(), logger, limite.getIp());
                        }
                    } else {
                        logger.log("Cpu = false");
                        logger.log("Checando alerta (cpu");
                        cpu = processador.checarEGerarAlerta("Cpu", c.getCpu(), limite.getLimiteCpu(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome(),logger, limite.getIp());
                    }

                    if (cpu != null) {
                        logger.log("Alerta cpu:" + cpu);
                        logger.log(cpu.getTipo());
                        logger.log("SLACK");
                       slack.enviarAlertas(cpu);

                        if (cpu.getTipo().equals("Critico")) {
                            if (estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()) {
                                logger.log("concatenar, alerta ativo");
                                estadoAlerta.setCpu(true);

                                estadoAlerta.setTimestamp(capturaTimestamp);

                                // CONCATENAR
                               concatenar.concatenarTicketsCriticos(cpu, estadoAlerta.getIdJira());

                                logger.log("CONCATENAR");
                            } else {
                                logger.log("Nenhum alerta ativo");

                                if ( estadoAlerta.getTimestamp() == null || timestampEstado.plusMinutes(3).isAfter(c.getTimestamp())) {

                                    logger.log("Fora dos 3 min, configura novo ticket");
                                    // Configura novo ticket
                                    // Criar novo Ticket
                                    // retorna o id do ticket do jira
                                    logger.log("NOVO TICKET");
                                   String idJira = jira.criarTicketsCriticos(cpu, logger);
                                    // atualiza json com novo estado - atualizar o objeto


                                    estadoAlerta = new EstadoAtual(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, capturaTimestamp, idJira, timestampTituloStr);
                                    String arquivoHistorico = fk_modelo + "-" + fk_zona + "-" + fk_empresa + "-" + estadoAlerta.getIdJira() + ".csv";
                                    // Cria novo csv de histórico
                                    lerHistorico(arquivoHistorico,logger);

                                    historico.clear();

                                    estadoAlerta.setCpu(true);
                                    estadoAlerta.setTimestamp(capturaTimestamp);
                                } else if(timestampEstado.plusMinutes(3).isBefore(c.getTimestamp())){
                                    // Não configura
                                    // CONCATENAR
                                    logger.log("Também concatena");
                                    logger.log("CONCATENAR");
                                    estadoAlerta.setCpu(true);
                                    estadoAlerta.setTimestamp(capturaTimestamp);
                                    concatenar.concatenarTicketsCriticos(cpu, estadoAlerta.getIdJira());
                                }

                            }
                        }

                    }

                    logger.log("Estado (ram):" + estadoAlerta.getRam());
                    if (estadoAlerta.getRam()) {
                        logger.log("(ram) = true");

                        if (c.getRam() > limite.getLimiteRam()) {

                            logger.log("Ultrapassou o limite ((ram))");

                            estadoAlerta.setTimestamp(capturaTimestamp);

                            logger.log("Estado atual timestamp (ram):"+estadoAlerta.getTimestamp());
                            ram = null;
                        } else {
                            logger.log("(ram) n ultrapassa o limite");
                            estadoAlerta.setRam(false);

                            logger.log("Checando alerta (ram)");
                            ram = processador.checarEGerarAlerta("Ram", c.getRam(), limite.getLimiteRam(),
                                    limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                    c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome(), logger, limite.getIp());
                        }
                    } else {
                        logger.log("(ram) = false");
                        logger.log("Checando alerta (ram)");
                        ram = processador.checarEGerarAlerta("Ram", c.getRam(), limite.getLimiteRam(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome(),logger, limite.getIp());
                    }

                    if (ram != null) {
                        logger.log("Alerta (ram):" + ram);
                        logger.log(ram.getTipo());
                        logger.log("SLACK (ram)");
                        slack.enviarAlertas(ram);

                        if (ram.getTipo().equals("Critico")) {
                            if (estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()) {
                                logger.log("concatenar, alerta ativo (ram)");
                                estadoAlerta.setRam(true);

                                estadoAlerta.setTimestamp(capturaTimestamp);

                                // CONCATENAR
                                concatenar.concatenarTicketsCriticos(ram, estadoAlerta.getIdJira());

                                logger.log("CONCATENAR(ram)");
                            } else {
                                logger.log("Nenhum alerta ativo (ram)");

                                if ( estadoAlerta.getTimestamp() == null || timestampEstado.plusMinutes(3).isAfter(c.getTimestamp())) {

                                    logger.log("Fora dos 3 min, configura novo ticket (ram)");
                                    // Configura novo ticket
                                    // Criar novo Ticket
                                    // retorna o id do ticket do jira
                                    logger.log("NOVO TICKET (ram)");
                                    String idJira = jira.criarTicketsCriticos(ram, logger);
                                    // atualiza json com novo estado - atualizar o objeto


                                    estadoAlerta = new EstadoAtual(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, capturaTimestamp, idJira, timestampTituloStr);
                                    String arquivoHistorico = fk_modelo + "-" + fk_zona + "-" + fk_empresa + "-" + estadoAlerta.getIdJira() + ".csv";
                                    // Cria novo csv de histórico
                                    lerHistorico(arquivoHistorico,logger);

                                    historico.clear();

                                    estadoAlerta.setRam(true);
                                    estadoAlerta.setTimestamp(capturaTimestamp);
                                } else if(timestampEstado.plusMinutes(3).isBefore(c.getTimestamp())){
                                    // Não configura
                                    // CONCATENAR
                                    logger.log("Também concatena (ram)");
                                    logger.log("CONCATENAR (ram)");
                                    estadoAlerta.setRam(true);
                                    estadoAlerta.setTimestamp(capturaTimestamp);
                                    concatenar.concatenarTicketsCriticos(ram, estadoAlerta.getIdJira());
                                }

                            }
                        }

                    }


                    logger.log("Estado (disco):" + estadoAlerta.getDisco());
                    if (estadoAlerta.getDisco()) {
                        logger.log("(disco) = true");

                        if (c.getDiscoUso() > limite.getLimiteDiscoUso()) {

                            logger.log("Ultrapassou o limite ((disco))");

                            estadoAlerta.setTimestamp(capturaTimestamp);

                            logger.log("Estado atual timestamp (disco):"+estadoAlerta.getTimestamp());
                            disco = null;
                        } else {
                            logger.log("(disco) n ultrapassa o limite");
                            estadoAlerta.setDisco(false);

                            logger.log("Checando alerta (disco)");
                            disco = processador.checarEGerarAlerta("Disco", c.getDiscoUso(), limite.getLimiteDiscoUso(),
                                    limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                    c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome(), logger, limite.getIp());
                        }
                    } else {
                        logger.log("(disco) = false");
                        logger.log("Checando alerta (disco)");
                        disco = processador.checarEGerarAlerta("Disco", c.getDiscoUso(), limite.getLimiteDiscoUso(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome(),logger, limite.getIp());
                    }

                    if (disco != null) {
                        logger.log("Alerta (disco):" + disco);
                        logger.log(disco.getTipo());
                        logger.log("SLACK (disco)");
                        slack.enviarAlertas(disco);

                        if (disco.getTipo().equals("Critico")) {
                            if (estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()) {
                                logger.log("concatenar, alerta ativo (disco)");
                                estadoAlerta.setDisco(true);

                                estadoAlerta.setTimestamp(capturaTimestamp);

                                // CONCATENAR
                                concatenar.concatenarTicketsCriticos(disco, estadoAlerta.getIdJira());

                                logger.log("CONCATENAR (disco)");
                            } else {
                                logger.log("Nenhum alerta ativo (disco)");

                                if ( estadoAlerta.getTimestamp() == null || timestampEstado.plusMinutes(3).isAfter(c.getTimestamp())) {

                                    logger.log("Fora dos 3 min, configura novo ticket (disco)");
                                    // Configura novo ticket
                                    // Criar novo Ticket
                                    // retorna o id do ticket do jira
                                    logger.log("NOVO TICKET (disco)");
                                    String idJira = jira.criarTicketsCriticos(disco, logger);
                                    // atualiza json com novo estado - atualizar o objeto


                                    estadoAlerta = new EstadoAtual(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, capturaTimestamp, idJira, timestampTituloStr);
                                    String arquivoHistorico = fk_modelo + "-" + fk_zona + "-" + fk_empresa + "-" + estadoAlerta.getIdJira() + ".csv";
                                    // Cria novo csv de histórico
                                    lerHistorico(arquivoHistorico,logger);

                                    historico.clear();

                                    estadoAlerta.setDisco(true);
                                    estadoAlerta.setTimestamp(capturaTimestamp);
                                } else if(timestampEstado.plusMinutes(3).isBefore(c.getTimestamp())){
                                    // Não configura
                                    // CONCATENAR
                                    logger.log("Também concatena (disco)");
                                    logger.log("CONCATENAR (disco)");
                                    estadoAlerta.setDisco(true);
                                    estadoAlerta.setTimestamp(capturaTimestamp);
                                    concatenar.concatenarTicketsCriticos(disco, estadoAlerta.getIdJira());
                                }

                            }
                        }

                    }

                    logger.log("Estado cpu:" + estadoAlerta.getGpu());
                    if (estadoAlerta.getGpu()) {
                        logger.log("(gpu) = true");

                        if (c.getGpu() > limite.getLimiteGpu()) {

                            logger.log("Ultrapassou o limite (gpu)");

                            estadoAlerta.setTimestamp(capturaTimestamp);

                            logger.log("Estado atual timestamp (gpu):"+estadoAlerta.getTimestamp());
                            gpu = null;
                        } else {
                            logger.log("Cpu n ultrapassa o limite (gpu)");
                            estadoAlerta.setGpu(false);

                            logger.log("Checando alerta (gpu)");
                            gpu = processador.checarEGerarAlerta("Gpu", c.getGpu(), limite.getLimiteGpu(),
                                    limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                    c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome(), logger, limite.getIp());
                        }
                    } else {
                        logger.log("(gpu) = false");
                        logger.log("Checando alerta (gpu)");
                        gpu = processador.checarEGerarAlerta("Gpu", c.getGpu(), limite.getLimiteGpu(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome(),logger, limite.getIp());
                    }

                    if (gpu != null) {
                        logger.log("Alerta cpu:" + gpu);
                        logger.log(gpu.getTipo());
                        logger.log("SLACK (gpu)");
                        slack.enviarAlertas(gpu);

                        if (gpu.getTipo().equals("Critico")) {
                            if (estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()) {
                                logger.log("concatenar, alerta ativo (gpu)");
                                estadoAlerta.setGpu(true);

                                estadoAlerta.setTimestamp(capturaTimestamp);

                                // CONCATENAR
                                concatenar.concatenarTicketsCriticos(gpu, estadoAlerta.getIdJira());

                                logger.log("CONCATENAR (gpu)");
                            } else {
                                logger.log("Nenhum alerta ativo (gpu)");

                                if ( estadoAlerta.getTimestamp() == null || timestampEstado.plusMinutes(3).isAfter(c.getTimestamp())) {

                                    logger.log("Fora dos 3 min, configura novo ticket (gpu)");
                                    // Configura novo ticket
                                    // Criar novo Ticket
                                    // retorna o id do ticket do jira
                                    logger.log("NOVO TICKET - GPU");
                                    String idJira = jira.criarTicketsCriticos(gpu,logger);
                                    // atualiza json com novo estado - atualizar o objeto


                                    estadoAlerta = new EstadoAtual(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, capturaTimestamp, idJira, timestampTituloStr);
                                    String arquivoHistorico = fk_modelo + "-" + fk_zona + "-" + fk_empresa + "-" + estadoAlerta.getIdJira() + ".csv";
                                    // Cria novo csv de histórico
                                    lerHistorico(arquivoHistorico,logger);

                                    historico.clear();

                                    estadoAlerta.setGpu(true);
                                    estadoAlerta.setTimestamp(capturaTimestamp);
                                } else if(timestampEstado.plusMinutes(3).isBefore(c.getTimestamp())){
                                    // Não configura
                                    // CONCATENAR
                                    logger.log("Também concatena GPU");
                                    logger.log("CONCATENAR GPU");
                                    estadoAlerta.setGpu(true);
                                    estadoAlerta.setTimestamp(capturaTimestamp);
                                    concatenar.concatenarTicketsCriticos(gpu, estadoAlerta.getIdJira());
                                }

                            }
                        }

                    }
                }

                if(estadoAlerta.getRam() || estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getGpu()){
                    logger.log("Algum alerta ativo");
                    // Atualizar csv de histórico atual usando o id, booleans do JSON E valores e timestamp da captura
                    HistoricoAlerta novoHistorico = new HistoricoAlerta(estadoAlerta.getCpu(), estadoAlerta.getRam(),
                            estadoAlerta.getGpu(), estadoAlerta.getDisco(),c.getCpu(), c.getGpu(),c.getDiscoUso(),c.getRam(),
                            c.getTimestamp(),false,p.getDowntime(),p.getCpu(),p.getRam(),p.getGpu());

                    historico.add(novoHistorico);
                }else{
                    logger.log("nenhum alerta ativo");

                    if(!historico.isEmpty() &&  historico.get(historico.size() - 1).getTimestamp().plusMinutes(3).isBefore(c.getTimestamp())){
                        logger.log("Historico cheio e tempo do ultimo timestamp esta dentro dos 3min");
                        HistoricoAlerta novoHistorico = new HistoricoAlerta(estadoAlerta.getCpu(), estadoAlerta.getRam(),
                                estadoAlerta.getGpu(), estadoAlerta.getDisco(),c.getCpu(), c.getGpu(),c.getDiscoUso(),c.getRam(),
                                c.getTimestamp(),false,p.getDowntime(),p.getCpu(),p.getRam(),p.getGpu());

                        historico.add(novoHistorico);

                    }else{
                        if(estadoAlerta.getIdJira() != null){
                           concatenar.mudar(estadoAlerta.getIdJira(), logger);
                           salvarCsvHistorico(fk_modelo,fk_zona,fk_empresa,estadoAlerta.getIdJira(),historico, logger);
                        }
                        // limpar JSON
                        logger.log("limoandi json");
                        estadoAlerta.setTimestamp(null);
                        estadoAlerta.setIdJira(null);
                    }

                }


            }
            // ESCREVER MEU CSV
            logger.log("fim do loop");
            logger.log("cpu" + estadoAlerta.getCpu());
            logger.log("ram" + estadoAlerta.getRam());
            logger.log("gpu" + estadoAlerta.getGpu());
            logger.log("disco" + estadoAlerta.getDisco());
            logger.log("Timestamp" + estadoAlerta.getTimestamp());
            logger.log("IdJira" + estadoAlerta.getIdJira());


            // ATUALIZAR MEU JSON E MANDAR PRO BUCKET
            logger.log("Escrevendo json estado atual");
            EstadoAtualWriter writerJson = new EstadoAtualWriter();
            writerJson.escreverEstadoAtual(jsonEstadoAtual,estadoAlerta,DESTINATION_BUCKET,s3Client);

            logger.log("-> Exportando capturas para o arquivo de saída:" + sourceKey);
            CsvWriter csvWriter = new CsvWriter();
            ByteArrayOutputStream csvOutputStream = csvWriter.writeCsv(capturas);

            InputStream csvInputStream = new ByteArrayInputStream(csvOutputStream.toByteArray());
            s3Client.putObject(DESTINATION_BUCKET, chaveDecodificadora, csvInputStream, null);

            s3InputStream.close();
            csvInputStream.close();
            csvOutputStream.close();
            logger.log("Processamento concluído");
            return "Processamento concluído;";
        }catch (Exception e) {
            context.getLogger().log("Erro: " + e.getMessage());
            logger.log(Arrays.toString(e.getStackTrace()));
            return "Erro no processamento";
        }

    }




    public void salvarCsvHistorico(String fk_modelo, String fk_zona,
            String fk_empresa, String idJira, List<HistoricoAlerta> historico, LambdaLogger logger){


        CsvHistoricoWriter writer = new CsvHistoricoWriter();
        String nome_arquivo_historico = fk_modelo + "-" + fk_zona + "-" + fk_empresa + "-" + idJira + ".csv";
        writer.escreverCsv(DESTINATION_BUCKET,s3Client,historico,nome_arquivo_historico,logger);

    }
    public List<HistoricoAlerta> lerHistorico(String nome_arquivo_historico, LambdaLogger logger){
        logger.log("lendo historico ou criando um novo");
        CsvHistoricoReader leitor = new CsvHistoricoReader();



        List<HistoricoAlerta> historico = leitor.leExibeArquivoCsv(nome_arquivo_historico,s3Client,DESTINATION_BUCKET, logger);

        return historico;

    }
}

