package school.sptech.cortex.monitoramento.modelo;

import java.time.LocalDateTime;

public class Alerta {
        private final String tipo;// Ex: "CRÍTICO", "ATENÇÃO", "IMPORTANTE"
        private  final  String fk_modelo;
        private  final  String fk_zona;
        private  final  String fk_empresa;
        private final String nomeModelo;
        private final String hostname;
        private final String tipoMetrica; // Ex: CPU, RAM, Disco
        private final double valorAtual;
        private final LocalDateTime timestamp; // Para facilitar a formatação no Slack/Jira

    public Alerta(String tipo, String fk_modelo, String fk_zona, String fk_empresa, String nomeModelo, String hostname, String tipoMetrica, double valorAtual, LocalDateTime timestamp) {
        this.tipo = tipo;
        this.fk_modelo = fk_modelo;
        this.fk_zona = fk_zona;
        this.fk_empresa = fk_empresa;
        this.nomeModelo = nomeModelo;
        this.hostname = hostname;
        this.tipoMetrica = tipoMetrica;
        this.valorAtual = valorAtual;
        this.timestamp = timestamp;
    }

    public String getHostname() {
        return hostname;
    }

    public String getTipo() {
        return tipo;
    }

    public String getFk_modelo() {
        return fk_modelo;
    }

    public String getFk_zona() {
        return fk_zona;
    }

    public String getFk_empresa() {
        return fk_empresa;
    }

    public String getNomeModelo() {
        return nomeModelo;
    }

    public String getTipoMetrica() {
        return tipoMetrica;
    }

    public double getValorAtual() {
        return valorAtual;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Alerta{" +
                "tipo='" + tipo + '\'' +
                ", fk_modelo='" + fk_modelo + '\'' +
                ", fk_zona='" + fk_zona + '\'' +
                ", fk_empresa='" + fk_empresa + '\'' +
                ", nomeModelo='" + nomeModelo + '\'' +
                ", hostname='" + hostname + '\'' +
                ", tipoMetrica='" + tipoMetrica + '\'' +
                ", valorAtual=" + valorAtual +
                ", timestamp=" + timestamp +
                '}';
    }
}
