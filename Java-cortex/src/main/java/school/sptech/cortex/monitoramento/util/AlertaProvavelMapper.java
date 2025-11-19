package school.sptech.cortex.monitoramento.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;


public class AlertaProvavelMapper {
    public List<AlertaProvavel> mapearAlertasProvaveis(InputStream inputStream) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        List<AlertaProvavel> alertaProvavelJson = objectMapper.readValue(
                inputStream, new TypeReference<List<AlertaProvavel>>() {
                });
        return alertaProvavelJson;

    }
}
