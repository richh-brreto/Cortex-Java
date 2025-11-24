package school.sptech.cortex.monitoramento.util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MapperAlertaProvavel {
    public List<AlertaProvavel> map(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, new TypeReference<List<AlertaProvavel>>() {
        });
    }
}
