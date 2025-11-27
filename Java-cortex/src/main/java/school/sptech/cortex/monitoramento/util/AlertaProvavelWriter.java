package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;


public class AlertaProvavelWriter {

    public static void escreverAlertasProvaveis(String nomeArquivo, List<AlertaProvavel> alertasProvavel, String trusted, AmazonS3 s3Client) {
        ObjectMapper mapper = new ObjectMapper();

        try{

            String conteudoAlertaProvavel = mapper.writeValueAsString(alertasProvavel);
            InputStream json = new ByteArrayInputStream(conteudoAlertaProvavel.getBytes("UTF-8"));

            s3Client.putObject(trusted, nomeArquivo, json, null);

        }catch (Exception erro){
            System.out.println("Erro ao escrever arquivo");
            System.exit(1);
        }
    }

}
