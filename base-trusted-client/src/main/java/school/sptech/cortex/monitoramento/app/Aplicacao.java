package school.sptech.cortex.monitoramento.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import school.sptech.cortex.monitoramento.modelo.Historico;
import school.sptech.cortex.monitoramento.modelo.Json;
import school.sptech.cortex.monitoramento.util.CsvHistoricoReader;
import school.sptech.cortex.monitoramento.util.MapperJson;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Aplicacao implements RequestHandler<S3Event, String> {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private static final String DESTINATION_BUCKET = "cortex-client-s3-bck";
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");


    @Override
    public String handleRequest(S3Event s3Event, Context context) {

        LambdaLogger logger = context.getLogger();

        logger.log("=================================================");
        logger.log("   CORTEX - INICIANDO PROCESSO DE MONITORAMENTO  ");
        logger.log("=================================================");


        String sourceBucket = s3Event.getRecords().get(0).getS3().getBucket().getName();
        String sourceKey = s3Event.getRecords().get(0).getS3().getObject().getKey();

        logger.log("Key:" + sourceKey);

        try {
            logger.log("Pegando arquivo");
            InputStream s3InputStream = s3Client.getObject(sourceBucket, sourceKey).getObjectContent();
            logger.log("Lendo arquivo");
            CsvHistoricoReader reader = new CsvHistoricoReader();
            List<Historico> capturas = reader.leExibeArquivoCsv(s3InputStream);

            if(capturas.isEmpty()){
                logger.log("capturas vazio");
                return null;
            }

            logger.log("Percorrendo historico");
            List<Double> cpu = new ArrayList<>();
            List<Double> ram = new ArrayList<>();
            List<Double> gpu = new ArrayList<>();
            List<Double> disco = new ArrayList<>();
            List<Double> cpuProc = new ArrayList<>();
            List<Double> gpuProc = new ArrayList<>();
            List<Double> ramProc = new ArrayList<>();
            List<Boolean> cpuBoolean = new ArrayList<>();
            List<Boolean> ramBoolean = new ArrayList<>();
            List<Boolean> discoBoolean = new ArrayList<>();
            List<Boolean> gpuBoolean = new ArrayList<>();
            List<String> timestamp = new ArrayList<>();
            List<Boolean> downtimeServer = new ArrayList<>();
            List<Boolean> downtimeProcesso = new ArrayList<>();

            boolean auxCpu = false;
            boolean auxRam = false;
            boolean auxDisco = false;
            boolean auxGpu = false;
            boolean auxServer = false;
            boolean auxProcesso = false;
            logger.log("criando linha do tempo");
            List<String> linha_do_tempo = new ArrayList<>();

            for(Historico h : capturas){
                String timestampStr =  h.getTimestamp().format(FORMATADOR_TIMESTAMP);
               cpu.add(h.getValorCpu());
                ram.add(h.getValorRam());
                gpu.add(h.getValorGpu());
                disco.add(h.getValorDisco());
                cpuProc.add(h.getCpu_processo());
                gpuProc.add(h.getGpu_processo());
                ramProc.add(h.getRam_processo());
                cpuBoolean.add(h.getCpu());
                ramBoolean.add(h.getRam());
                gpuBoolean.add(h.getGpu());
                discoBoolean.add(h.getDisco());
                timestamp.add(timestampStr);
                downtimeServer.add(h.getDowntimeServer());
                downtimeProcesso.add(h.getDowntimeProcesso());


                String frase = "";
                if(!(auxCpu == h.getCpu())){
                    if(!auxCpu){
                       frase = "Início CPU - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxCpu = true;
                    }else {
                        frase = "Fim CPU - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxCpu = false;
                    }
                }

                if(!(auxRam == h.getRam())){
                    if(!auxRam){
                        frase = "Início RAM - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxRam = true;
                    }else {
                        frase = "Fim RAM - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxRam = false;
                    }
                }

                if(!(auxDisco == h.getDisco())){
                    if(!auxDisco){
                        frase = "Início Disco - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxDisco = true;
                    }else {
                        frase = "Fim Disco - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxDisco = false;
                    }
                }

                if(!(auxGpu == h.getGpu())){
                    if(!auxGpu){
                        frase = "Início GPU - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxGpu = true;
                    }else {
                        frase = "Fim GPU - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxGpu = false;
                    }
                }
                if(!(auxServer == h.getDowntimeServer())){
                    if(!auxServer){
                        frase = "Início Downtime Servidor - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxServer = true;
                    }else {
                        frase = "Fim Downtime Servidor - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxServer = false;
                    }
                }
                if(!(auxProcesso == h.getDowntimeProcesso()) && !h.getDowntimeServer()){
                    if(!auxProcesso){
                        frase = "Início Downtime Processo - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxProcesso = true;
                    }else {
                        frase = "Fim Downtime Processo - " + timestampStr;
                        linha_do_tempo.add(frase);
                        auxProcesso = false;
                    }
                }
            }

            logger.log("calculando duração");
            String duracao_em_alerta = calcularDuracaoTotal(capturas.get(0).getTimestamp(),capturas.get(capturas.size() - 1).getTimestamp());

            logger.log("calculando downtime server");
            String downtime_servidor = downtimeServerCalcular(downtimeServer, timestamp);

            logger.log("calculando downtime processo");
            String downtime_processo = downtimeProcessoCalcular(downtimeProcesso,timestamp);

            logger.log("duracao cpu");
            String duracao_cpu = downtimeProcessoCalcular(cpuBoolean,timestamp);

            logger.log("duracao ram");
            String duracao_ram = downtimeProcessoCalcular(ramBoolean,timestamp);

            logger.log("duracao disco");
            String duracao_disco = downtimeProcessoCalcular(discoBoolean,timestamp);

            logger.log("duracao gpu");
            String duracao_gpu = downtimeProcessoCalcular(gpuBoolean,timestamp);


            logger.log("Criando objeto");
            Json j = new Json(cpu,ram,disco,gpu,cpuProc,gpuProc, ramProc,cpuBoolean,ramBoolean,gpuBoolean,
                    discoBoolean,timestamp,duracao_em_alerta, downtime_servidor, downtime_processo, linha_do_tempo,
                    duracao_cpu, duracao_ram,duracao_disco,duracao_gpu);


            logger.log("Escrevendo json");
            InputStream salvarJson = MapperJson.escreverJson(j);

            logger.log("Dividindo título");
            String[] divisao01 = sourceKey.split("/");
            String[] divisao02 = divisao01[1].split("\\.");

            logger.log(divisao02[0] + ".json" + " Mandando para o S3");
            s3Client.putObject(DESTINATION_BUCKET, "marilia/" + divisao02[0] + ".json", salvarJson, null);

            return "Processamento concluído;";
        }catch (Exception e) {
            context.getLogger().log("Erro: " + e.getMessage());
            logger.log(Arrays.toString(e.getStackTrace()));
            return "Erro no processamento";
        }

    }
    public static String calcularDuracaoTotal(LocalDateTime primeiro, LocalDateTime ultimo) {
        if (primeiro == null || ultimo == null) {
            return "00:00:00";
        }

        // 3. Calcula a duração
        Duration duracao = Duration.between(primeiro, ultimo);

        // 4. Converte a duração para o formato HH:mm:ss
        long segundosTotais = duracao.getSeconds();

        long horas = segundosTotais / 3600;
        long minutos = (segundosTotais % 3600) / 60;
        long segundos = segundosTotais % 60;

        // Formata com preenchimento zero (ex: 5 -> 05)
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    public static String downtimeServerCalcular(List<Boolean> downtimes, List<String> timestamp){



        List<Integer> indice = new ArrayList<>();
        for(int i = 0; i < downtimes.size(); i ++){
            if(downtimes.get(i)){
                indice.add(i);
            }
        }

        if(indice.isEmpty()){
            return "00:00:00";
        }

        LocalDateTime primeiro = LocalDateTime.parse(timestamp.get(indice.get(0)), FORMATADOR_TIMESTAMP);
        LocalDateTime ultimo = LocalDateTime.parse(timestamp.get(indice.get(indice.size() - 1)), FORMATADOR_TIMESTAMP);

       return calcularDuracaoTotal(primeiro, ultimo);
    }

    public static String downtimeProcessoCalcular(List<Boolean> downtimes, List<String> timestamp){


            // 1. Coleta os timestamps apenas dos momentos onde o downtime é true
            List<LocalDateTime> timestampsDowntime = new ArrayList<>();
            for (int i = 0; i < downtimes.size(); i++) {
                if (downtimes.get(i)) {
                    try {
                        timestampsDowntime.add(LocalDateTime.parse(timestamp.get(i), FORMATADOR_TIMESTAMP));
                    } catch (Exception e) {
                        // Tratar erro de parsing se necessário
                        System.err.println("Erro ao analisar o timestamp: " + timestamp.get(i));
                    }
                }
            }

            if (timestampsDowntime.isEmpty()) {
                return "00:00:00";
            }

            long segundosTotais = 0;

            // 2. Agrupa e calcula a duração
            LocalDateTime inicioSequencia = null;
            LocalDateTime ultimoTimestampSequencia = null;

            for (LocalDateTime daVez : timestampsDowntime) {
                if (inicioSequencia == null) {
                    // Início da primeira sequência
                    inicioSequencia = daVez;
                    ultimoTimestampSequencia = daVez;
                } else {
                    // Verifica se o timestamp atual continua a sequência (intervalo <= 80s)
                    if (ultimoTimestampSequencia.plusSeconds(80).isAfter(daVez) ||
                            ultimoTimestampSequencia.plusSeconds(80).isEqual(daVez)) {

                        // Continua a sequência: atualiza apenas o último timestamp
                        ultimoTimestampSequencia = daVez;
                    } else {
                        // Fim da sequência anterior (intervalo > 80s): calcula e zera
                        Duration duracao = Duration.between(inicioSequencia, ultimoTimestampSequencia);
                        segundosTotais += duracao.getSeconds();

                        // Inicia nova sequência
                        inicioSequencia = daVez;
                        ultimoTimestampSequencia = daVez;
                    }
                }
            }

            // 3. Processa a última sequência (ESSENCIAL, faltou no código original)
            if (inicioSequencia != null) {
                Duration duracao = Duration.between(inicioSequencia, ultimoTimestampSequencia);
                segundosTotais += duracao.getSeconds();
            }

            // 4. Formata o resultado
            long horas = segundosTotais / 3600;
            long minutos = (segundosTotais % 3600) / 60;
            long segundos = segundosTotais % 60;

            return String.format("%02d:%02d:%02d", horas, minutos, segundos);

    }
}

