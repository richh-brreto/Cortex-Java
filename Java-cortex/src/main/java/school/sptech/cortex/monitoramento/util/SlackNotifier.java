package school.sptech.cortex.monitoramento.util;

import school.sptech.cortex.monitoramento.modelo.Alerta;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class SlackNotifier {
    private static final String SLACK_WEBHOOK_URL = ConfiguracaoAmbiente.get("SLACK_WEBHOOK_URL");

    // O cliente HTTP é criado uma vez e reutilizado
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    /**
     * Envia a lista de alertas (CRÍTICO, ATENÇÃO, PREDITIVO) para o canal do Slack via Webhook.
     */
    public void enviarAlertas(List<Alerta> alertas) {
        if (alertas == null || alertas.isEmpty()) {
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
    private String formatarMensagem(List<Alerta> alertas) {
        // Usamos o Hostname da primeira captura para identificar a máquina
        Alerta primeiroAlerta = alertas.get(0);

        // Constrói o cabeçalho da mensagem
        String cabecalho = String.format(
                ":robot_face: *Monitoramento Cortex: Novo Lote de Alertas*\n" +
                        ":computer: Máquina: `%s:%s`\n\n" +
                        "*Detalhes dos Alertas:*\n",
                primeiroAlerta.getIp(),
                primeiroAlerta.getHostname()
        );

        // Usa StringBuilder para otimizar a concatenação dentro do loop
        StringBuilder detalhes = new StringBuilder();

        // Itera sobre a lista de alertas usando um loop for simples (sem Stream/Map)
        for (Alerta alerta : alertas) {
            String linhaAlerta = String.format(
                    "%s *%s* - `%s`: %.2f%% (Limite: %.1f%%) - %s",
                    getEmojiByTipo(alerta.getTipo()),
                    alerta.getTipo(),
                    alerta.getTipoMetrica(),
                    alerta.getValorAtual(),
                    alerta.getLimite(),
                    alerta.getTimestamp()
            );

            detalhes.append(linhaAlerta).append("\n");
        }

        // Retorna o cabeçalho concatenado com a lista de detalhes
        return cabecalho + detalhes.toString();
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

    /**
     * Retorna o emoji correspondente ao tipo de alerta.
     */
    private String getEmojiByTipo(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "CRÍTICO" -> ":fire:";
            case "ATENÇÃO" -> ":warning:";
            case "PREDITIVO" -> ":large_orange_diamond:";
            default -> ":information_source:";
        };
    }

    /**
     * Envia a requisição HTTP POST para a URL do Webhook do Slack.
     */
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
            System.err.println("❌ ERRO GERAL na requisição HTTP para o Slack: " + e.getMessage());
        }
    }
}
