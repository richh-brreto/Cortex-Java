package school.sptech.cortex.monitoramento.modelo;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import java.time.LocalDateTime;

public class CapturaProcesso {
    @CsvBindByPosition(position = 0)
    private String ip;

    @CsvBindByPosition(position = 1)
    private String hostname;

    @CsvBindByPosition(position = 2)
    @CsvDate(value = "yyyy-MM-dd_HH-mm-ss")
    private LocalDateTime timestamp;

    @CsvBindByPosition(position = 3)
    private String processo;

    @CsvBindByPosition(position = 4)
    private Double cpu;

    @CsvBindByPosition(position = 5)
    private Double ram;

    @CsvBindByPosition(position = 6)
    private Double dadosGravados;

    @CsvBindByPosition(position = 7)
    private String mac;

    @CsvBindByPosition(position = 8)
    private Double gpu;

    @CsvBindByPosition(position = 9)
    private Double discoUso;

    public CapturaProcesso(String ip, String hostname, LocalDateTime timestamp, String processo, Double cpu, Double ram, Double dadosGravados, String mac, Double gpu, Double discoUso) {
        this.ip = ip;
        this.hostname = hostname;
        this.timestamp = timestamp;
        this.processo = processo;
        this.cpu = cpu;
        this.ram = ram;
        this.dadosGravados = dadosGravados;
        this.mac = mac;
        this.gpu = gpu;
        this.discoUso = discoUso;
    }

    public CapturaProcesso() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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
