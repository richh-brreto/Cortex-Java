package school.sptech.cortex.monitoramento.modelo;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import java.time.LocalDateTime;

public class CapturaProcesso {
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
    private String processo;

    @CsvBindByPosition(position = 5)
    private Double cpu;

    @CsvBindByPosition(position = 6)
    private Double ram;

    @CsvBindByPosition(position = 7)
    private Double dadosGravados;

    @CsvBindByPosition(position = 8)
    private Double gpu;

    @CsvBindByPosition(position = 9)
    private Double discoUso;

    public CapturaProcesso() {
    }

    public CapturaProcesso(String fk_modelo, String fk_zona, String fk_empresa, LocalDateTime timestamp, String processo, Double cpu, Double ram, Double dadosGravados, Double gpu, Double discoUso) {
        this.fk_modelo = fk_modelo;
        this.fk_zona = fk_zona;
        this.fk_empresa = fk_empresa;
        this.timestamp = timestamp;
        this.processo = processo;
        this.cpu = cpu;
        this.ram = ram;
        this.dadosGravados = dadosGravados;
        this.gpu = gpu;
        this.discoUso = discoUso;
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

    public String getProcesso() {
        return processo;
    }

    public void setProcesso(String processo) {
        this.processo = processo;
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

    public Double getDadosGravados() {
        return dadosGravados;
    }

    public void setDadosGravados(Double dadosGravados) {
        this.dadosGravados = dadosGravados;
    }

    public Double getGpu() {
        return gpu;
    }

    public void setGpu(Double gpu) {
        this.gpu = gpu;
    }

    public Double getDiscoUso() {
        return discoUso;
    }

    public void setDiscoUso(Double discoUso) {
        this.discoUso = discoUso;
    }
}
