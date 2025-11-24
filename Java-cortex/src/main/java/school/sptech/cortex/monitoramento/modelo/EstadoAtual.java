package school.sptech.cortex.monitoramento.modelo;

import java.time.LocalDateTime;

public class EstadoAtual {
    private Boolean cpu;
    private Boolean ram;
    private Boolean gpu;
    private Boolean disco;
    private LocalDateTime timestamp;
    private String idJira;

    public EstadoAtual(Boolean cpu, Boolean ram, Boolean gpu, Boolean disco, LocalDateTime timestamp, String idJira) {
        this.cpu = cpu;
        this.ram = ram;
        this.gpu = gpu;
        this.disco = disco;
        this.timestamp = timestamp;
        this.idJira = idJira;
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

    public Boolean getGpu() {
        return gpu;
    }

    public void setGpu(Boolean gpu) {
        this.gpu = gpu;
    }

    public Boolean getDisco() {
        return disco;
    }

    public void setDisco(Boolean disco) {
        this.disco = disco;
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
}
