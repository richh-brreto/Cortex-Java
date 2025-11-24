package school.sptech.cortex.monitoramento.modelo;

import java.time.LocalDateTime;

public class HistoricoAlerta {
    private Boolean cpu;
    private Boolean ram;
    private Boolean gpu;
    private  Boolean disco;
    private Double valorCpu;
    private Double valorGpu;
    private  Double valorDisco;
    private Double valorRam;
    private LocalDateTime timestamp;

    public HistoricoAlerta(Boolean cpu, Boolean ram, Boolean gpu, Boolean disco, Double valorCpu, Double valorGpu, Double valorDisco, Double valorRam, LocalDateTime timestamp) {
        this.cpu = cpu;
        this.ram = ram;
        this.gpu = gpu;
        this.disco = disco;
        this.valorCpu = valorCpu;
        this.valorGpu = valorGpu;
        this.valorDisco = valorDisco;
        this.valorRam = valorRam;
        this.timestamp = timestamp;
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

    public Double getValorCpu() {
        return valorCpu;
    }

    public void setValorCpu(Double valorCpu) {
        this.valorCpu = valorCpu;
    }

    public Double getValorGpu() {
        return valorGpu;
    }

    public void setValorGpu(Double valorGpu) {
        this.valorGpu = valorGpu;
    }

    public Double getValorDisco() {
        return valorDisco;
    }

    public void setValorDisco(Double valorDisco) {
        this.valorDisco = valorDisco;
    }

    public Double getValorRam() {
        return valorRam;
    }

    public void setValorRam(Double valorRam) {
        this.valorRam = valorRam;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
