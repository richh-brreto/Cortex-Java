package school.sptech.cortex.monitoramento.modelo;

import java.util.ArrayList;
import java.util.List;

public class Json {
    private List<Double> cpu;
    private List<Double> ram;
    private List<Double> disco;
    private List<Double> gpu;
    private List<Double> cpuProc;
    private List<Double> ramProc;
    private List<Double> gpuProc;
    private List<Boolean> cpuBoolean;
    private List<Boolean> ramBoolean;
    private List<Boolean> gpuBoolean;
    private List<Boolean> discoBoolean;
    private List<String> timestamp;
    private String duracao_alerta;
    private String downtime_servidor;
    private String downtime_processo;
    private List<String> linha_do_tempo;
    private String duracao_cpu;
    private String duracao_ram;
    private String duracao_disco;
    private String duracao_gpu;

    public Json(){
        this.cpu = new ArrayList<>();
        this.ram = new ArrayList<>();
        this.gpu = new ArrayList<>();
        this.disco = new ArrayList<>();
        this.cpuProc = new ArrayList<>();
        this.ramProc = new ArrayList<>();
        this.gpuProc = new ArrayList<>();
        this.discoBoolean = new ArrayList<>();
        this.cpuBoolean = new ArrayList<>();
        this.ramBoolean = new ArrayList<>();
        this.gpuBoolean = new ArrayList<>();
        this.timestamp = new ArrayList<>();
        this.linha_do_tempo = new ArrayList<>();
    }

    public Json(List<Double> cpu, List<Double> ram, List<Double> disco, List<Double> gpu, List<Double> cpuProc, List<Double> ramProc, List<Double> gpuProc, List<Boolean> cpuBoolean, List<Boolean> ramBoolean, List<Boolean> gpuBoolean, List<Boolean> discoBoolean, List<String> timestamp, String duracao_alerta, String downtime_servidor, String downtime_processo, List<String> linha_do_tempo, String duracao_cpu, String duracao_ram, String duracao_disco, String duracao_gpu) {
        this.cpu = cpu;
        this.ram = ram;
        this.disco = disco;
        this.gpu = gpu;
        this.cpuProc = cpuProc;
        this.ramProc = ramProc;
        this.gpuProc = gpuProc;
        this.cpuBoolean = cpuBoolean;
        this.ramBoolean = ramBoolean;
        this.gpuBoolean = gpuBoolean;
        this.discoBoolean = discoBoolean;
        this.timestamp = timestamp;
        this.duracao_alerta = duracao_alerta;
        this.downtime_servidor = downtime_servidor;
        this.downtime_processo = downtime_processo;
        this.linha_do_tempo = linha_do_tempo;
        this.duracao_cpu = duracao_cpu;
        this.duracao_ram = duracao_ram;
        this.duracao_disco = duracao_disco;
        this.duracao_gpu = duracao_gpu;
    }

    public String getDuracao_cpu() {
        return duracao_cpu;
    }

    public void setDuracao_cpu(String duracao_cpu) {
        this.duracao_cpu = duracao_cpu;
    }

    public String getDuracao_ram() {
        return duracao_ram;
    }

    public void setDuracao_ram(String duracao_ram) {
        this.duracao_ram = duracao_ram;
    }

    public String getDuracao_disco() {
        return duracao_disco;
    }

    public void setDuracao_disco(String duracao_disco) {
        this.duracao_disco = duracao_disco;
    }

    public String getDuracao_gpu() {
        return duracao_gpu;
    }

    public void setDuracao_gpu(String duracao_gpu) {
        this.duracao_gpu = duracao_gpu;
    }

    public List<Double> getCpu() {
        return cpu;
    }

    public void setCpu(List<Double> cpu) {
        this.cpu = cpu;
    }

    public List<Double> getRam() {
        return ram;
    }

    public void setRam(List<Double> ram) {
        this.ram = ram;
    }

    public List<Double> getDisco() {
        return disco;
    }

    public void setDisco(List<Double> disco) {
        this.disco = disco;
    }

    public List<Double> getGpu() {
        return gpu;
    }

    public void setGpu(List<Double> gpu) {
        this.gpu = gpu;
    }

    public List<Double> getCpuProc() {
        return cpuProc;
    }

    public void setCpuProc(List<Double> cpuProc) {
        this.cpuProc = cpuProc;
    }

    public List<Double> getRamProc() {
        return ramProc;
    }

    public void setRamProc(List<Double> ramProc) {
        this.ramProc = ramProc;
    }

    public List<Double> getGpuProc() {
        return gpuProc;
    }

    public void setGpuProc(List<Double> gpuProc) {
        this.gpuProc = gpuProc;
    }

    public List<Boolean> getCpuBoolean() {
        return cpuBoolean;
    }

    public void setCpuBoolean(List<Boolean> cpuBoolean) {
        this.cpuBoolean = cpuBoolean;
    }

    public List<Boolean> getRamBoolean() {
        return ramBoolean;
    }

    public void setRamBoolean(List<Boolean> ramBoolean) {
        this.ramBoolean = ramBoolean;
    }

    public List<Boolean> getGpuBoolean() {
        return gpuBoolean;
    }

    public void setGpuBoolean(List<Boolean> gpuBoolean) {
        this.gpuBoolean = gpuBoolean;
    }

    public List<Boolean> getDiscoBoolean() {
        return discoBoolean;
    }

    public void setDiscoBoolean(List<Boolean> discoBoolean) {
        this.discoBoolean = discoBoolean;
    }

    public List<String> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(List<String> timestamp) {
        this.timestamp = timestamp;
    }

    public String getDuracao_alerta() {
        return duracao_alerta;
    }

    public void setDuracao_alerta(String duracao_alerta) {
        this.duracao_alerta = duracao_alerta;
    }

    public String getDowntime_servidor() {
        return downtime_servidor;
    }

    public void setDowntime_servidor(String downtime_servidor) {
        this.downtime_servidor = downtime_servidor;
    }

    public String getDowntime_processo() {
        return downtime_processo;
    }

    public void setDowntime_processo(String downtime_processo) {
        this.downtime_processo = downtime_processo;
    }

    public List<String> getLinha_do_tempo() {
        return linha_do_tempo;
    }

    public void setLinha_do_tempo(List<String> linha_do_tempo) {
        this.linha_do_tempo = linha_do_tempo;
    }
}
