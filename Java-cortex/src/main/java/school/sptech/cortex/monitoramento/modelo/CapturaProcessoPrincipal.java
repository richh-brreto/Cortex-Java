package school.sptech.cortex.monitoramento.modelo;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import java.time.LocalDateTime;

public class CapturaProcessoPrincipal {

    private LocalDateTime timestamp;
    private Double cpu;
    private Double ram;
    private Double gpu;
    private Boolean downtime;

    public CapturaProcessoPrincipal(LocalDateTime timestamp, Double cpu, Double ram, Double gpu, Boolean downtime) {
        this.timestamp = timestamp;
        this.cpu = cpu;
        this.ram = ram;
        this.gpu = gpu;
        this.downtime = downtime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Double getRam() {
        return ram;
    }

    public void setRam(Double ram) {
        this.ram = ram;
    }

    public Double getGpu() {
        return gpu;
    }

    public void setGpu(Double gpu) {
        this.gpu = gpu;
    }

    public Boolean getDowntime() {
        return downtime;
    }

    public void setDowntime(Boolean downtime) {
        this.downtime = downtime;
    }
}
