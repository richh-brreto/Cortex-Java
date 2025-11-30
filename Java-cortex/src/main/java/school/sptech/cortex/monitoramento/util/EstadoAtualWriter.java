package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.modelo.EstadoAtual;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

public class EstadoAtualWriter {
    public static void escreverEstadoAtual(String nomeArquivo, EstadoAtual novoEstado, String trusted, AmazonS3 s3Client) {
        ObjectMapper mapper = new ObjectMapper();

        try{

            String conteudoEstado = mapper.writeValueAsString(novoEstado);
            InputStream json = new ByteArrayInputStream(conteudoEstado.getBytes("UTF-8"));

            s3Client.putObject(trusted, nomeArquivo, json, null);

        }catch (Exception erro){
            System.out.println("Erro ao escrever arquivo");
            System.exit(1);
        }
    }
    public static void escreverEstadoAtual(String nomeArquivo, String trusted, AmazonS3 s3Client) {
        ObjectMapper mapper = new ObjectMapper();

        try{
            LocalDateTime agora = LocalDateTime.now();
            EstadoAtual novo = new EstadoAtual(false,false,false,false,agora,null,null);
            String conteudoEstado = mapper.writeValueAsString(novo);
            InputStream json = new ByteArrayInputStream(conteudoEstado.getBytes("UTF-8"));

            s3Client.putObject(trusted, nomeArquivo, json, null);

        }catch (Exception erro){
            System.out.println("Erro ao escrever arquivo");
            System.exit(1);
        }
    }
}
