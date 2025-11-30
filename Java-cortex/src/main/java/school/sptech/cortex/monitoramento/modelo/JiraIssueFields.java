package school.sptech.cortex.monitoramento.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssueFields {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("labels")
    private List<String> labels;

    @JsonProperty("description")
    private Map<String, Object> description;

    public JiraIssueFields() {}

    public JiraIssueFields(String summary, List<String> labels, Map<String, Object> description) {
        this.summary = summary;
        this.labels = labels;
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Map<String, Object> getDescription() {
        return description;
    }

    public void setDescription(Map<String, Object> description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "JiraIssueFields{" +
                "summary='" + summary + '\'' +
                ", labels=" + labels +
                ", description=" + description +
                '}';
    }
}
