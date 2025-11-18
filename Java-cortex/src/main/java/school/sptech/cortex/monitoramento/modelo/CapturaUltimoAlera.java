package school.sptech.cortex.monitoramento.modelo;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import java.time.LocalDateTime;

public class CapturaSistema {
    @CsvBindByPosition(position = 0)
    private String ip;

    @CsvBindByPosition(position = 1)
    private String hostname;

    @CsvBindByPosition(position = 2)
    @CsvDate(value = "yyyy-MM-dd_HH-mm-ss")
    private LocalDateTime ultimo_alerta_timestamp;

    @CsvBindByPosition(position = 3)
    private Double id_jira;


    @CsvBindByPosition(position = 5)
    private Double armazenamento; // Uso de Armazenamento em %

    @CsvBindByPosition(position = 6)
    private Double discoUso; // Uso de Disco em %

    @CsvBindByPosition(position = 7)
    private String mac;

    @CsvBindByPosition(position = 8)
    private Double gpu; // Uso de GPU em %

    public CapturaSistema(String ip, String hostname, LocalDateTime timestamp, Double cpu, Double ram, Double armazenamento, Double discoUso, String mac, Double gpu) {
        this.ip = ip;
        this.hostname = hostname;
        this.timestamp = timestamp;
        this.cpu = cpu;
        this.ram = ram;
        this.armazenamento = armazenamento;
        this.discoUso = discoUso;
        this.mac = mac;
        this.gpu = gpu;
    }

    public CapturaSistema() {
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

    @Override
    public String toString() {
        return "CapturaSistema{" +
                "ip='" + ip + '\'' +
                ", hostname='" + hostname + '\'' +
                ", timestamp=" + timestamp +
                ", cpu=" + cpu +
                ", ram=" + ram +
                ", armazenamento=" + armazenamento +
                ", discoUso=" + discoUso +
                ", mac='" + mac + '\'' +
                ", gpu=" + gpu +
                '}';
    }
}
