package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.modelo.EstadoAtual;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EstadoAtualWriter {
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    public static void escreverEstadoAtual(String nomeArquivo, EstadoAtual novoEstado, String trusted, AmazonS3 s3Client) {
        ObjectMapper mapper = new ObjectMapper();

        try{

            String conteudoEstado = mapper.writeValueAsString(novoEstado);
            InputStream json = new ByteArrayInputStream(conteudoEstado.getBytes("UTF-8"));

            s3Client.putObject(trusted, "arquivos_apoio/" + nomeArquivo, json, null);

        }catch (Exception erro){
            System.out.println("Erro ao escrever arquivo");

        }
    }
    public static EstadoAtual escreverEstadoAtualNovo(String nomeArquivo, String trusted, AmazonS3 s3Client, LambdaLogger logger) {
        ObjectMapper mapper = new ObjectMapper();

        try{
            logger.log("Crando novo json estado atual");

            EstadoAtual novo = new EstadoAtual(false,false,false,false,null,null,null);
            String conteudoEstado = mapper.writeValueAsString(novo);
            InputStream json = new ByteArrayInputStream(conteudoEstado.getBytes("UTF-8"));

            s3Client.putObject(trusted, "arquivos_apoio/" + nomeArquivo, json, null);

            return novo;
        }catch (Exception erro){
            logger.log("Erro ao escrever arquivo");
            throw  new RuntimeException("Falha ao criar EstadoAtual", erro);
        }
    }
}
