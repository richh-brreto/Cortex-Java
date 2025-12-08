package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.modelo.EstadoAtual;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.List;

public class EstadoAtualChecar {

    public EstadoAtual checarEstadoAtual(String nomeArquivo, AmazonS3 s3Client, String trusted, LambdaLogger logger){

        try {
            logger.log("Pegando objeto estado atual");
            InputStream jsonEstado = s3Client.getObject(trusted,"arquivos_apoio/"+ nomeArquivo).getObjectContent();

            MapperEstadoAtual mapperEstadoAtual = new MapperEstadoAtual();
            logger.log("Transformando em objeto");
            EstadoAtual estadoAtual = mapperEstadoAtual.map(jsonEstado);

            return estadoAtual;

        } catch (Exception e) {

            boolean isUnrecoverableS3Error = (e instanceof AmazonS3Exception) && ((AmazonS3Exception) e).getStatusCode() != 404;
            if (isUnrecoverableS3Error) {
                logger.log("ERRO IRRECUPERÁVEL DE S3 (não 404): " + e.getMessage());
                throw new RuntimeException("Falha de S3 ao ler estado atual.", e);
            }
            logger.log("Arquivo Estado atual não encontrado (404). Criando novo buffer...");


            try {
                logger.log("Criando");
                EstadoAtualWriter writer = new EstadoAtualWriter();
               EstadoAtual novo = writer.escreverEstadoAtualNovo(nomeArquivo, trusted, s3Client, logger);

                logger.log(novo.toString());
                if (novo == null) {
                    throw new RuntimeException("Falha ao criar o EstadoAtual no S3, o novo");
                }
                return novo;

            } catch (Exception e2) {
                throw new RuntimeException("Falha ao criar o EstadoAtual no S3, o novo");
            }
        }
    }
}
