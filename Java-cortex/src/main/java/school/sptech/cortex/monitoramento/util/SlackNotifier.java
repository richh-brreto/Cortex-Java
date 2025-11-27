package school.sptech.cortex.monitoramento.util;

import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.Parametro;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class SlackNotifier {
    private static final String SLACK_WEBHOOK_URL = ConfiguracaoAmbiente.get("SLACK_WEBHOOK_URL");
    private static final java.time.format.DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    // O cliente HTTP é criado uma vez e reutilizado
    private final HttpClient httpClient = HttpClient.newBuilder().build();


    public void enviarAlertas(Alerta alertas) {
        if (alertas == null) {
            return;
        }

        try {
            // 1. Formata os objetos Alerta em uma String Markdown
            String markdownText = formatarMensagem(alertas);

            // 2. Monta o corpo JSON que o Slack espera
            String jsonPayload = montarJsonPayload(markdownText);

            // 3. Envia a requisição HTTP
            enviarRequisicao(jsonPayload);

        } catch (Exception e) {
            System.err.println("ERRO FATAL ao enviar alertas para o Slack: " + e.getMessage());
        }
    }

    /**
     * Converte a lista de Alertas em uma String formatada em Slack Markdown
     */
    private String formatarMensagem(Alerta alertas) {
        // Usamos o Hostname da primeira captura para identificar a máquina
        String notificacao = null;
        if (alertas.getTipo().equals("Atencao")){
             notificacao = String.format(
                    "ATENÇÃO! Modelo %s que está na Máquina %s está com a %s em %.2f%% em %s",
                    alertas.getNomeModelo().toUpperCase(),
                    alertas.getHostname().toUpperCase(),
                    alertas.getTipoMetrica().toUpperCase(),
                    alertas.getValorAtual(),
                    alertas.getTimestamp().format(FORMATADOR_TIMESTAMP)
            );
        }else {
           notificacao = String.format(
                    "CRÍTICO! Modelo %s que está na Máquina %s está com a %s em %.2f%% em %s",
                    alertas.getNomeModelo().toUpperCase(),
                    alertas.getHostname().toUpperCase(),
                    alertas.getTipoMetrica().toUpperCase(),
                    alertas.getValorAtual(),
                    alertas.getTimestamp().format(FORMATADOR_TIMESTAMP)
            );
        }
        // Constrói o cabeçalho da mensagem

        return notificacao;
    }

    /**
     * Monta o JSON para o payload do Slack.
     */
    private String montarJsonPayload(String texto) {
        // Escapa as aspas duplas no texto para serem válidas dentro do JSON
        String textoEscapado = texto.replace("\"", "\\\"");
        // Payload mínimo do Slack: {"text": "conteúdo"}
        return String.format("{\"text\": \"%s\"}", textoEscapado);
    }

    private void enviarRequisicao(String jsonPayload) {
        try {
            if (SLACK_WEBHOOK_URL == null || SLACK_WEBHOOK_URL.contains("SUA_URL_DE_WEBHOOK_AQUI")) {
                System.err.println("ALERTA: A URL do Slack Webhook não foi configurada. Envio ignorado.");
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SLACK_WEBHOOK_URL))
                    // O Slack exige que o Content-Type seja application/json
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Envia a requisição e recebe a resposta do servidor do Slack
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("-> Sucesso: Mensagem de alerta enviada ao Slack.");
            } else {
                System.err.println("-> Falha no envio para o Slack. Código: " + response.statusCode());
                System.err.println("   Corpo de Resposta: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("ERRO GERAL na requisição HTTP para o Slack: " + e.getMessage());
        }
    }
}
