package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.s3.AmazonS3;
import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.modelo.EstadoAtual;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

public class EstadoAtualChecar {

    public EstadoAtual checarEstadoAtual(String nomeArquivo, AmazonS3 s3Client, String trusted){

        try {
            InputStream jsonEstado = s3Client.getObject(trusted, nomeArquivo).getObjectContent();

            MapperEstadoAtual mapperEstadoAtual = new MapperEstadoAtual();

            EstadoAtual estadoAtual = mapperEstadoAtual.map(jsonEstado);

            return estadoAtual;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
