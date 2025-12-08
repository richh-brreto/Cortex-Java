package school.sptech.cortex.monitoramento.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;


public class AlertaProvavel {
    @JsonProperty("timestamp")
    private String timestamp;

    // Mapeia o campo "document" do JSON para o atributo "cpf"
    @JsonProperty("medida")
    private Double medida;

    public AlertaProvavel(){}

    public AlertaProvavel(String timestamp, Double medida) {
        this.timestamp = timestamp;
        this.medida = medida;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getMedida() {
        return medida;
    }

    public void setMedida(Double medida) {
        this.medida = medida;
    }
}
