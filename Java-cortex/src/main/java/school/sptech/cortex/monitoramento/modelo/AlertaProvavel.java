package school.sptech.cortex.monitoramento.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;


public class AlertaProvavel {
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    // Mapeia o campo "document" do JSON para o atributo "cpf"
    @JsonProperty("medida")
    private Double medida;

    public AlertaProvavel(){}

    public AlertaProvavel(LocalDateTime timestamp, Double medida) {
        this.timestamp = timestamp;
        this.medida = medida;
    }

    @Override
    public String toString() {
        return "alertaProvavel{" +
                "timestamp=" + timestamp +
                ", medida=" + medida +
                '}';
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getMedida() {
        return medida;
    }

    public void setMedida(Double medida) {
        this.medida = medida;
    }
}
