package school.sptech.cortex.monitoramento.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssueGet {

    @JsonProperty("fields")
    private JiraIssueFields fields;

    public JiraIssueGet(JiraIssueFields fields) {
        this.fields = fields;
    }

    public JiraIssueGet(){};

    @Override
    public String toString() {
        return "JiraIssueGet{" +
                "fields=" + fields +
                '}';
    }

    public JiraIssueFields getFields() {
        return fields;
    }

    public void setFields(JiraIssueFields fields) {
        this.fields = fields;
    }
}
