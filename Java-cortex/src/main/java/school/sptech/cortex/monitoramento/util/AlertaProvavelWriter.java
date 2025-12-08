package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;

import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class AlertaProvavelWriter {

    public static void escreverAlertasProvaveis(String nomeArquivo, List<AlertaProvavel> alertasProvavel, String trusted, AmazonS3 s3Client, LambdaLogger logger) {
        ObjectMapper mapper = new ObjectMapper();

        try{

            String conteudoAlertaProvavel = mapper.writeValueAsString(alertasProvavel);
            logger.log("Conteudo alerta provavel: "+conteudoAlertaProvavel);
            byte[] bytes = conteudoAlertaProvavel.getBytes(StandardCharsets.UTF_8);
            InputStream json = new ByteArrayInputStream(conteudoAlertaProvavel.getBytes("UTF-8"));


            s3Client.putObject(trusted, "arquivos_apoio/" + nomeArquivo, json, null);
            logger.log("Arquico escrito");
        }catch (Exception erro){
            logger.log("Erro ao escrever arquivo" + erro);

        }
    }

}
