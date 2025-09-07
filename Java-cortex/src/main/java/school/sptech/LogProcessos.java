package school.sptech;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogProcessos {

    private LocalDateTime timestamp;
    private String processo;
    private Integer pid;
    private Double cpu;
    private Double ram;

    public LogProcessos (String timestamp, String processo, Integer pid, Double cpu, Double ram) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
        LocalDateTime dataTime = LocalDateTime.parse(timestamp, formatter);
        this.timestamp = dataTime;
        this.processo = processo;
        this.pid = pid;
        this.cpu = cpu;
        this.ram = ram;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
        return " timestamp = " + timestamp.format(formatter) +
                ", processo = '" + processo + '\'' +
                ", pid = " + pid +
                ", cpu = " + cpu +
                "%, ram = " + ram +
                "% \n ";
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

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
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

}
