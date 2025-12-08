package school.sptech.cortex.monitoramento.service;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.util.AlertaProvavelWriter;
import school.sptech.cortex.monitoramento.util.MapperAlertaProvavel;


import java.io.*;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProcessadorDeCapturasService {

    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final MapperAlertaProvavel mapperAlertaProvavel= new MapperAlertaProvavel();

    public Alerta checarEGerarAlerta(
            String componente,
            Double valorAtual,
            Double limiteCritico, // Recebemos apenas o limite CRÍTICO do banco
            Integer tempoBaseMinutos,
            LocalDateTime timestamp,
            AmazonS3 s3Client,
            String trusted,
            String fk_modelo,
            String fk_zona,
            String fk_empresa,
            String hostname,
            String nomeModelo,
            LambdaLogger logger,
            String ip

    ) {
        // Validação básica
        logger.log("limiteCritico:" + limiteCritico);
        logger.log("tempoBase:" + tempoBaseMinutos);

        if (limiteCritico == null || tempoBaseMinutos == null || tempoBaseMinutos <= 0) return null;

        // 1. CÁLCULO DA REGRA DE NEGÓCIO
        // Limite de Atenção: Limite Crítico - 10%
        Double limiteAtencao = limiteCritico * 0.90;

        // Tempo de Atenção: Metade do tempo base
        Integer tempoAtencaoMinutos = Math.max(1, tempoBaseMinutos / 2); // Garante que seja pelo menos 1 minuto

        logger.log("LimiteAtencao:" + limiteAtencao);
        logger.log("tempoBaseAtencao:" + tempoAtencaoMinutos);

        logger.log("Valor atual:" + valorAtual);
        // 2. ANÁLISE CRÍTICA (Usa Limite Crítico e Tempo Base)
        if (valorAtual >= limiteCritico) {
            logger.log("Valor atual passou limite Critico");
            logger.log("Checando tempo...");
            // LOGICA DE CRITICO CHECANDO O JSON
            // LEMBRAR DE SETAR O TIPO DE ALERTA
            return checarTempo(valorAtual,timestamp,tempoBaseMinutos,"Critico", s3Client,trusted, fk_modelo,componente,fk_empresa,fk_zona,nomeModelo, hostname, logger, ip);
            // 3. ANÁLISE DE ATENÇÃO (Usa Limite de Atenção e Tempo de Atenção)
        }else if (valorAtual >= limiteAtencao) {
            logger.log("Valor atual passou limite Atencao");
            logger.log("Checando tempo...");
            // LOGICA DE ATENCAO CHECANDO O JSON
            // LEMBRAR DE SETAR O TIPO DE ALERTA
            return checarTempo(valorAtual,timestamp,tempoAtencaoMinutos,"Atencao", s3Client,trusted, fk_modelo,componente,fk_empresa,fk_zona,nomeModelo, hostname, logger, ip);
        }
        logger.log("Não gerou alertas - retorna Null");
        // 5. Geração do Alerta
        return null;
    }

    public AlertaProvavel criarAlertaProvavel(Double medida, LocalDateTime timestamp){

        AlertaProvavel alertaProvavel = new AlertaProvavel();

        alertaProvavel.setMedida(medida);
        String timestampStr = timestamp.format(FORMATADOR_TIMESTAMP);
        alertaProvavel.setTimestamp(timestampStr);

        return  alertaProvavel;
    }

    public Alerta checarTempo(
                              Double valorAtual,
                              LocalDateTime timestamp,
                              Integer tempoBaseMinutos,
                              String tipo,
                              AmazonS3 s3Client,
                              String trusted,
                              String fk_modelo,
                              String componente,
                              String fk_empresa,
                              String fk_zona,
                              String nomeModelo,
                              String hostname,
                              LambdaLogger logger,
                              String ip) {

        logger.log("Função chedcar Tempo");
        String jsonPossibilidade = fk_modelo + "-" + componente + "-" + tipo + "-Possibilidade.json";


        List<AlertaProvavel> listaAlertasProvaveis = new ArrayList<>();

        try (InputStream arquivoPossibilidade = s3Client.getObject(trusted, "arquivos_apoio/" + jsonPossibilidade).getObjectContent()) {


            logger.log("arquivo pego com sucesso");


            listaAlertasProvaveis = mapperAlertaProvavel.map(arquivoPossibilidade);

            Integer tamanho = listaAlertasProvaveis.size();
            logger.log("tamanho:" + tamanho);
            String ultimoTimestampStr = listaAlertasProvaveis.get(tamanho - 1).getTimestamp();
            logger.log("ultimo TimestampStr:" + ultimoTimestampStr);
            LocalDateTime ultimoTimestamp = LocalDateTime.parse(ultimoTimestampStr, FORMATADOR_TIMESTAMP);

            if (timestamp.minusSeconds(80).isAfter(ultimoTimestamp) || timestamp.minusSeconds(80).equals(ultimoTimestamp)) {
            logger.log("Ultimo timestamp é 80 segundos ou mais depois do timestamp atual");
                listaAlertasProvaveis.clear();
                listaAlertasProvaveis.add(criarAlertaProvavel(valorAtual, timestamp));
                AlertaProvavelWriter.escreverAlertasProvaveis(jsonPossibilidade, listaAlertasProvaveis, trusted, s3Client, logger);

                return null;

            } else {
                logger.log("Mesmo alerta provavel");
                //faz parte, adicionar nova captura, verifica tempo total - verifica se ultrapassa limite
                listaAlertasProvaveis.add(criarAlertaProvavel(valorAtual, timestamp));
                    AlertaProvavelWriter.escreverAlertasProvaveis(jsonPossibilidade, listaAlertasProvaveis, trusted, s3Client, logger);

                 logger.log("Verificar se há um novo alerta");
                tamanho = listaAlertasProvaveis.size();
                ultimoTimestamp =  LocalDateTime.parse(listaAlertasProvaveis.get(tamanho - 1).getTimestamp(), FORMATADOR_TIMESTAMP) ;
                String primeiroStr = listaAlertasProvaveis.get(0).getTimestamp();
                LocalDateTime primeiro = LocalDateTime.parse(primeiroStr, FORMATADOR_TIMESTAMP);
                if (primeiro.plusMinutes(tempoBaseMinutos).isBefore(ultimoTimestamp)) {
                    logger.log("Configura novo alerta");
                    // ultrapassa
                    listaAlertasProvaveis.clear();
                    AlertaProvavelWriter.escreverAlertasProvaveis(jsonPossibilidade, listaAlertasProvaveis, trusted, s3Client, logger);
                    Alerta novoalerta = new Alerta(tipo, fk_modelo, fk_zona, fk_empresa, nomeModelo, hostname, componente ,valorAtual, timestamp, ip);

                    return novoalerta;
                } else {
                    logger.log("não retorna novo alerta");
                    // n ultrapassa
                    return null;
                }
            }
        } catch (Exception e) {


            boolean isUnrecoverableS3Error = (e instanceof AmazonS3Exception) && ((AmazonS3Exception) e).getStatusCode() != 404;
            if (isUnrecoverableS3Error) {
                logger.log("ERRO IRRECUPERÁVEL DE S3 (não 404): " + e.getMessage());
                throw new RuntimeException("Falha de S3 ao ler alertas prováveis.", e);
            }
            logger.log("Arquivo de alerta provável não encontrado (404). Criando novo buffer...");

            try {
                listaAlertasProvaveis.clear();
                listaAlertasProvaveis.add(criarAlertaProvavel(valorAtual, timestamp));
                AlertaProvavelWriter.escreverAlertasProvaveis(jsonPossibilidade,listaAlertasProvaveis , trusted, s3Client, logger);

                return null;

            } catch (Exception e2) {
                logger.log(e2.getMessage());
                throw new RuntimeException("Não foi possível criar o arquivo de alertas prváveis", e2);
            }


        }

    }
}
