package school.sptech.cortex.monitoramento.modelo;

public class Alerta {
        private final String tipo; // Ex: "CRÍTICO", "ATENÇÃO", "PREDITIVO"
        private final String hostname;
        private final String ip;
        private final String tipoMetrica; // Ex: CPU, RAM, Disco
        private final double valorAtual;
        private final double limite;
        private final String timestamp; // Para facilitar a formatação no Slack/Jira

        public Alerta(String tipo, String hostname, String ip, String tipoMetrica, double valorAtual, double limite, String timestamp) {
            this.tipo = tipo;
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
            return String.format("[%s] Máquina %s (%s): %s em %.2f%% (Limite: %.2f%%) em %s",
                    tipo, hostname, ip, tipoMetrica, valorAtual, limite, timestamp);
        }
}
