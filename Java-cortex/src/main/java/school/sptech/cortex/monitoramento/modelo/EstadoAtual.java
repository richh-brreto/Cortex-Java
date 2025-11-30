package school.sptech.cortex.monitoramento.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class EstadoAtual {

    @JsonProperty("cpu")
    private Boolean cpu;

    // Mapeia o campo "document" do JSON para o atributo "cpf"
    @JsonProperty("ram")
    private Boolean ram;

    @JsonProperty("disco")
    private Boolean disco;

    @JsonProperty("gpu")
    private Boolean gpu;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("idJira")
    private String idJira;

    @JsonProperty("ultimoTimestamp")
    private LocalDateTime ultimoTimestamp;

    public EstadoAtual(Boolean cpu, Boolean ram, Boolean disco, Boolean gpu, LocalDateTime timestamp, String idJira, LocalDateTime ultimoTimestamp) {
        this.cpu = cpu;
        this.ram = ram;
        this.disco = disco;
        this.gpu = gpu;
        this.timestamp = timestamp;
        this.idJira = idJira;
        this.ultimoTimestamp = ultimoTimestamp;
    }

    public Boolean getCpu() {
        return cpu;
    }

    public void setCpu(Boolean cpu) {
        this.cpu = cpu;
    }

    public Boolean getRam() {
        return ram;
    }

    public void setRam(Boolean ram) {
        this.ram = ram;
    }

    public Boolean getDisco() {
        return disco;
    }

    public void setDisco(Boolean disco) {
        this.disco = disco;
    }

    public Boolean getGpu() {
        return gpu;
    }

    public void setGpu(Boolean gpu) {
        this.gpu = gpu;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIdJira() {
        return idJira;
    }

    public void setIdJira(String idJira) {
        this.idJira = idJira;
    }

    public LocalDateTime getUltimoTimestamp() {
        return ultimoTimestamp;
    }

    public void setUltimoTimestamp(LocalDateTime ultimoTimestamp) {
        this.ultimoTimestamp = ultimoTimestamp;
    }
}
