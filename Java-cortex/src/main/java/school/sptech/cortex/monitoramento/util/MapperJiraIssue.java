package school.sptech.cortex.monitoramento.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.modelo.JiraIssueFields;
import school.sptech.cortex.monitoramento.modelo.JiraIssueGet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MapperJiraIssue {
    public JiraIssueGet map(String inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, new TypeReference<JiraIssueGet>() {
        });
    }
}
