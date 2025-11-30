package school.sptech.cortex.monitoramento.modelo;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import java.time.LocalDateTime;

public class CapturaSistema {
    @CsvBindByPosition(position = 0)
    private String fk_modelo;

    @CsvBindByPosition(position = 1)
    private String fk_zona;

    @CsvBindByPosition(position = 2)
    private String fk_empresa;

    @CsvBindByPosition(position = 3)
    @CsvDate(value = "yyyy-MM-dd_HH-mm-ss")
    private LocalDateTime timestamp;

    @CsvBindByPosition(position = 4)
    private Double cpu; // Uso de CPU em %

    @CsvBindByPosition(position = 5)
    private Double ram; // Uso de RAM em %

    @CsvBindByPosition(position = 6)
    private Double armazenamento; // Uso de Armazenamento em %

    @CsvBindByPosition(position = 7)
    private Double discoUso; // Uso de Disco em %

    @CsvBindByPosition(position = 8)
    private Double gpu; // Uso de GPU em %

    public CapturaSistema(String fk_modelo, String fk_zona, String fk_empresa, LocalDateTime timestamp, Double cpu, Double ram, Double armazenamento, Double discoUso, Double gpu) {
        this.fk_modelo = fk_modelo;
        this.fk_zona = fk_zona;
        this.fk_empresa = fk_empresa;
        this.timestamp = timestamp;
        this.cpu = cpu;
        this.ram = ram;
        this.armazenamento = armazenamento;
        this.discoUso = discoUso;
        this.gpu = gpu;
    }

    public CapturaSistema() {
    }


    public String getFk_modelo() {
        return fk_modelo;
    }

    public void setFk_modelo(String fk_modelo) {
        this.fk_modelo = fk_modelo;
    }

    public String getFk_zona() {
        return fk_zona;
    }

    public void setFk_zona(String fk_zona) {
        this.fk_zona = fk_zona;
    }

    public String getFk_empresa() {
        return fk_empresa;
    }

    public void setFk_empresa(String fk_empresa) {
        this.fk_empresa = fk_empresa;
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

    public Double getArmazenamento() {
        return armazenamento;
    }

    public void setArmazenamento(Double armazenamento) {
        this.armazenamento = armazenamento;
    }

    public Double getDiscoUso() {
        return discoUso;
    }

    public void setDiscoUso(Double discoUso) {
        this.discoUso = discoUso;
    }

    public Double getGpu() {
        return gpu;
    }

    public void setGpu(Double gpu) {
        this.gpu = gpu;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "CapturaSistema{" +
                "fk_modelo='" + fk_modelo + '\'' +
                ", fk_zona='" + fk_zona + '\'' +
                ", fk_empresa='" + fk_empresa + '\'' +
                ", timestamp=" + timestamp +
                ", cpu=" + cpu +
                ", ram=" + ram +
                ", armazenamento=" + armazenamento +
                ", discoUso=" + discoUso +
                ", gpu=" + gpu +
                '}';
    }
}