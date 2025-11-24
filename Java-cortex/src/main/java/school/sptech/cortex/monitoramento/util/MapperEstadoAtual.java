package school.sptech.cortex.monitoramento.util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.modelo.EstadoAtual;

import java.io.IOException;
import java.io.InputStream;


public class MapperEstadoAtual {
    public EstadoAtual map(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, new TypeReference<EstadoAtual>() {
        });
    }
}
