package school.sptech.cortex.monitoramento.modelo;

public class Parametro {
    private Integer id;
    private String nomeEmpresa;
    private String nome;
    private String ip;
    private String hostname;
    private Double limiteCpu;
    private Double limiteRam;
    private Double limiteDiscoUso;
    private Double limiteGpu;
    private Integer tempoParametroMin;

    public Parametro(Integer id, String nomeEmpresa, String nome, String ip, String hostname, Double limiteCpu, Double limiteRam, Double limiteDiscoUso, Double limiteGpu, Integer tempoParametroMin) {
        this.id = id;
        this.nomeEmpresa = nomeEmpresa;
        this.nome = nome;
        this.ip = ip;
        this.hostname = hostname;
        this.limiteCpu = limiteCpu;
        this.limiteRam = limiteRam;
        this.limiteDiscoUso = limiteDiscoUso;
        this.limiteGpu = limiteGpu;
        this.tempoParametroMin = tempoParametroMin;
    }

    public Parametro() {
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public Double getLimiteCpu() {
        return limiteCpu;
    }

    public void setLimiteCpu(Double limiteCpu) {
        this.limiteCpu = limiteCpu;
    }

    public Double getLimiteRam() {
        return limiteRam;
    }

    public void setLimiteRam(Double limiteRam) {
        this.limiteRam = limiteRam;
    }

    public Double getLimiteDiscoUso() {
        return limiteDiscoUso;
    }

    public void setLimiteDiscoUso(Double limiteDiscoUso) {
        this.limiteDiscoUso = limiteDiscoUso;
    }

    public Double getLimiteGpu() {
        return limiteGpu;
    }

    public void setLimiteGpu(Double limiteGpu) {
        this.limiteGpu = limiteGpu;
    }

    public Integer getTempoParametroMin() {
        return tempoParametroMin;
    }

    public void setTempoParametroMin(Integer tempoParametroMin) {
        this.tempoParametroMin = tempoParametroMin;
    }
}