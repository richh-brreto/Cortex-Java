package school.sptech.cortex.monitoramento.app;


import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import school.sptech.cortex.monitoramento.dao.LimiteDAO;
import school.sptech.cortex.monitoramento.modelo.*;
import school.sptech.cortex.monitoramento.service.ProcessadorDeCapturasService;
import school.sptech.cortex.monitoramento.util.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class Aplicacao implements RequestHandler<S3Event, String> {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private static final String DESTINATION_BUCKET = "trusted-stocks";

    @Override
    public String handleRequest(S3Event s3Event, Context context) {

        System.out.println("=================================================");
        System.out.println("   CORTEX - INICIANDO PROCESSO DE MONITORAMENTO  ");
        System.out.println("=================================================");


        String sourceBucket = s3Event.getRecords().get(0).getS3().getBucket().getName();
        String sourceKey = s3Event.getRecords().get(0).getS3().getObject().getKey();

        // 1. LEITURA E CARREGAMENTO DOS DADOS DO CSV
        System.out.printf("\n[1] Lendo capturas do arquivo: %s%n");


        try {
            InputStream s3InputStream = s3Client.getObject(sourceBucket, sourceKey).getObjectContent();

            CsvReader reader = new CsvReader();
            List<CapturaSistema> capturas = reader.lerECarregarCapturasSistema(s3InputStream);

            // AQUI VAI COMEÇAR O TRATAMENTO DE ALERTAS
            checarAlerta(capturas);


            System.out.printf("-> Exportando capturas para o arquivo de saída: %s%n", sourceKey);
            CsvWriter csvWriter = new CsvWriter();
            ByteArrayOutputStream csvOutputStream = csvWriter.writeCsv(capturas);

            InputStream csvInputStream = new ByteArrayInputStream(csvOutputStream.toByteArray());
            s3Client.putObject(DESTINATION_BUCKET, sourceKey, csvInputStream, null);


            return "Processamento concluído;";
        }catch (Exception e) {
            context.getLogger().log("Erro: " + e.getMessage());
            return "Erro no processamento";
        }

    }


    public void checarAlerta(List<CapturaSistema> capturas){
        ProcessadorDeCapturasService processador = new ProcessadorDeCapturasService();

        LimiteDAO limiteComando = new LimiteDAO();
        Parametro limite = limiteComando.buscarLimitesPorMaquina(capturas.get(0).getFk_modelo());



        String fk_modelo = capturas.get(0).getFk_modelo();
        String fk_zona = capturas.get(0).getFk_zona();
        String fk_empresa = capturas.get(0).getFk_empresa();


        EstadoAtualChecar estado = new EstadoAtualChecar();
        String jsonEstadoAtual = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + "estadoAtual.json";
        EstadoAtual estadoAlerta = estado.checarEstadoAtual(jsonEstadoAtual, s3Client, DESTINATION_BUCKET);

        CsvHistoricoReader leitor = new CsvHistoricoReader();

        String nome_arquivo_historico = fk_modelo + ";" + fk_zona + ";" + fk_empresa + ";" + estadoAlerta.getIdJira() + "historico.csv";
        // Lembrar que se não existir vai retornar como null
        List<HistoricoAlerta> historico = leitor.leExibeArquivoCsv(nome_arquivo_historico,s3Client,DESTINATION_BUCKET);

        SlackNotifier slack = new SlackNotifier();
        JiraTicketCreator jira = new JiraTicketCreator();

        Alerta cpu = null;
        Alerta gpu = null;
        Alerta ram = null;
        Alerta disco = null;



        for(CapturaSistema c : capturas){


            // CPU
            if(estadoAlerta != null){
                if(estadoAlerta.getCpu()){
                    if(c.getCpu() > limite.getLimiteCpu()){
                        estadoAlerta.setTimestamp(c.getTimestamp());
                    }else {
                        estadoAlerta.setCpu(false);
                      cpu = processador.checarEGerarAlerta("Cpu", c.getCpu(), limite.getLimiteCpu(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                    }
                }else {
                  cpu =  processador.checarEGerarAlerta("Cpu", c.getCpu(), limite.getLimiteCpu(),
                            limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                            c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                }

                if(cpu != null){

                    slack.enviarAlertas(cpu);
                        if(cpu.getTipo().equalsIgnoreCase("Critico")){
                        if(estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getRam() || estadoAlerta.getGpu()){
                            estadoAlerta.setCpu(true);
                            estadoAlerta.setTimestamp(c.getTimestamp());
                            // CONCATENAR
                        }else{
                            if(estadoAlerta.getTimestamp().plusMinutes(3).isAfter(c.getTimestamp())){
                                // Configura novo ticket
                                // Criar novo Ticket
                                jira.criarTicketsCriticos(cpu);

                                // retorna o id do ticket do jira
                                // Cria novo csv de histórico
                                // atualiza json com novo estado - atualizar o objeto

                            }else {
                                // Não configura
                                // CONCATENAR
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
                      gpu =  processador.checarEGerarAlerta("Gpu", c.getGpu(), limite.getLimiteGpu(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                    }
                }else {
                    gpu = processador.checarEGerarAlerta("Gpu", c.getGpu(), limite.getLimiteGpu(),
                            limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                            c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                }
                if(estadoAlerta.getDisco()){
                    if(c.getDiscoUso() > limite.getLimiteDiscoUso()){
                        estadoAlerta.setTimestamp(c.getTimestamp());
                    }else {
                        estadoAlerta.setDisco(false);
                        disco = processador.checarEGerarAlerta("Disco", c.getDiscoUso(), limite.getLimiteDiscoUso(),
                                limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                                c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                    }
                }else{
                  disco =  processador.checarEGerarAlerta("Disco", c.getDiscoUso(), limite.getLimiteDiscoUso(),
                            limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                            c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                }
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
                    ram = processador.checarEGerarAlerta("Ram", c.getRam(), limite.getLimiteRam(),
                            limite.getTempoParametroMin(), c.getTimestamp(), s3Client, DESTINATION_BUCKET,
                            c.getFk_modelo(), c.getFk_zona(), c.getFk_empresa(), limite.getHostname(), limite.getNome());
                }
            }

            // PRECISO ATUALIZAR O JSON

            // CSV HISORICO

            if(estadoAlerta.getRam() || estadoAlerta.getCpu() || estadoAlerta.getDisco() || estadoAlerta.getGpu()){
                // Atualizar csv de histórico atual usando o id, booleans do JSON E valores e timestamp da captura
            }else{
            //    if(CSV.getTimestamp().plusMinutes(3).isAfter(c.getTimestamp())){
               //     return;
              //  }else{
                    // Atualizar csv de histórico atual usando o id, booleans do JSON E valores e timestamp da captura
              //  }
            }


        }

        // ATUALIZAR MEU JSON E MANDAR PRO BUCKET

    }

}
