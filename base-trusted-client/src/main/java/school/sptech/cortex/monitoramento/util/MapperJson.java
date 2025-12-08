package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.Historico;
import school.sptech.cortex.monitoramento.modelo.Json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MapperJson {
    public static InputStream escreverJson(Json json) throws  IOException {
        ObjectMapper mapper = new ObjectMapper();

        try{

            String conteudoJson = mapper.writeValueAsString(json);

            byte[] bytes = conteudoJson.getBytes(StandardCharsets.UTF_8);

            return new ByteArrayInputStream(bytes);

        }catch (Exception erro){
            System.out.println("Erro ao escrever arquivo");
            erro.printStackTrace();
            return null;
        }
    }

}
