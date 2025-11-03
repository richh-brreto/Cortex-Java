package school.sptech.cortex.monitoramento.modelo;

public class Alerta {
        private final String tipo; // Ex: "CRÍTICO", "ATENÇÃO", "IMPORTANTE"
        private final String nomeEmpresa;
        private final String nomeModelo;
        private final String hostname;
        private final String ip;
        private final String tipoMetrica; // Ex: CPU, RAM, Disco
        private final double valorAtual;
        private final double limite;
        private final String timestamp; // Para facilitar a formatação no Slack/Jira

        public Alerta(String tipo, String nomeEmpresa, String nomeModelo, String hostname, String ip, String tipoMetrica, double valorAtual, double limite, String timestamp) {
            this.tipo = tipo;
            this.nomeEmpresa = nomeEmpresa;
            this.nomeModelo = nomeModelo;
            this.hostname = hostname;
            this.ip = ip;
            this.tipoMetrica = tipoMetrica;
            this.valorAtual = valorAtual;
            this.limite = limite;
            this.timestamp = timestamp;
        }

        // --- Getters ---

        public String getTipo() {
            return tipo;
        }

    public String getNomeEmpresa() { return nomeEmpresa; }

    public String getNomeModelo() { return nomeModelo; }

    public String getHostname() {
            return hostname;
        }

        public String getIp() {
            return ip;
        }

        public String getTipoMetrica() {
            return tipoMetrica;
        }

        public double getValorAtual() {
            return valorAtual;
        }

        public double getLimite() {
            return limite;
        }

        public String getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("%s - [%s] Máquina %s (%s): %s em %.2f%% (Limite: %.2f%%) em %s",
                    nomeEmpresa, tipo, hostname, ip, tipoMetrica, valorAtual, limite, timestamp);
        }
}
