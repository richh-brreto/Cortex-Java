package school.sptech.cortex.monitoramento.util;


import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.Parametro;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class JiraTicketCreator {
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    // VARIÁVEIS DE CONFIGURAÇÃO
    private final String jiraUrl;
    private final String jiraUsername;
    private final String jiraApiToken;
    private final String jiraProjectKey;
    private final String jiraIssueTypeName;
    private final String jiraPriorityName;


    private final HttpClient httpClient;

    public JiraTicketCreator() {
        // Leitura das configurações
        this.jiraUrl = ConfiguracaoAmbiente.get("JIRA_URL");
        this.jiraUsername = ConfiguracaoAmbiente.get("JIRA_USERNAME");
        this.jiraApiToken = ConfiguracaoAmbiente.get("JIRA_API_TOKEN");
        this.jiraProjectKey = ConfiguracaoAmbiente.get("JIRA_PROJECT_KEY");
        this.jiraIssueTypeName = ConfiguracaoAmbiente.get("JIRA_ISSUE_TYPE_NAME");
        this.jiraPriorityName = ConfiguracaoAmbiente.get("JIRA_PRIORITY_NAME");


        // Verificações de configuração obrigatória
        Objects.requireNonNull(jiraUrl, "JIRA_URL não pode ser nulo.");
        Objects.requireNonNull(jiraUsername, "JIRA_USERNAME não pode ser nulo.");
        Objects.requireNonNull(jiraApiToken, "JIRA_API_TOKEN não pode ser nulo.");
        Objects.requireNonNull(jiraProjectKey, "JIRA_PROJECT_KEY não pode ser nulo.");
        Objects.requireNonNull(jiraIssueTypeName, "JIRA_ISSUE_TYPE_NAME não pode ser nulo.");
        Objects.requireNonNull(jiraPriorityName, "JIRA_PRIORITY_NAME não pode ser nulo.");


        // Configuração do Cliente HTTP com timeout de 10 segundos
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }


    public String criarTicketsCriticos(Alerta alertas) {

        System.out.println("--- Iniciando verificação de alertas CRÍTICOS para o Jira ---");

            // 1. FILTRAGEM

            try {

                String timestamp = alertas.getTimestamp().format(FORMATADOR_TIMESTAMP);
                // 2. Formata a mensagem de negócio para o Jira
                String summary = String.format("ALERTA CRÍTICO: Modelo %s Máquina %s - Última Atualização %s",
                        alertas.getNomeModelo(), alertas.getHostname(),timestamp);

                String description = String.format(
                        "Alerta de Utilização Crítica - Cortex\n\n" +
                                "O modelo: %s - na máquina: %s ultrapassou o limite crítico de:\n" +
                                "- %s  - com o uso de: %.2f%% - em: %s",
                        alertas.getNomeModelo(), alertas.getHostname(), alertas.getTipoMetrica(), alertas.getValorAtual(),
                        timestamp

                );

                String categoria = alertas.getTipoMetrica().toLowerCase();

                String identificador = alertas.getFk_modelo() + ";" + alertas.getFk_zona() + ";" + alertas.getFk_empresa();

                // 3. Monta o JSON de Payload
                String jsonPayload = buildIssueJson(summary, description, categoria, identificador);

                // 4. Envia a requisição
                String keyJira = enviarRequisicao(jsonPayload);

                if(keyJira != null){
                    return keyJira;
                }else {
                    return null;
                }



            } catch (Exception e) {
                System.err.println("ERRO ao processar Alerta para o Jira ");

                return null;
            }

    }

    // --- MÉTODOS PRIVADOS DE SUPORTE ---

    /**
     * Monta o JSON (Payload) para o Issue no formato ADF, incluindo campos obrigatórios.
     */
    private String buildIssueJson(String summary, String descriptionDetails, String categoria, String identificador) {
        String safeSummary = summary.replace("\"", "\\\"");
        String adfDescription = createAdfDescription(descriptionDetails);

        String jsonTemplate =
                "{" +
                        "\"fields\": {" +
                        "\"project\": {\"key\": \"%s\"}," +
                        "\"summary\": \"%s\"," +
                        "\"description\": %s," +
                        "\"issuetype\": {\"name\": \"%s\"}," +
                        "\"priority\": {\"name\": \"%s\"}," +
                        "\"labels\": [\"%s\"]," +
                        "\"customfield_10060\": {\"value\": \"%s\"}," +
                        "\"customfield_10093\": \"%s\"," +
                        "\"customfield_10059\": {\"value\": \"%s\"}," +
                        "\"customfield_10044\": {\"value\": \"%s\"}," +
                        "\"customfield_10039\": \"%s\"" +
                        "}" +
                        "}";

        return String.format(
                jsonTemplate,
                jiraProjectKey,
                safeSummary,
                adfDescription, // adfDescription é um JSON e é inserido sem aspas adicionais
                jiraIssueTypeName,
                jiraPriorityName,
                categoria,
                "Em Alerta (ticket mais atual)",
                identificador,
                "Incidente",
                "Critical",
               "Service requests"
        );
    }


    private String createAdfDescription(String simpleText) {
        // 1. Escapa caracteres que podem quebrar o JSON (principalmente aspas duplas e barras invertidas)
        String escapedText = simpleText
                .replace("\\", "\\\\") // Escapa barras invertidas
                .replace("\"", "\\\""); // Escapa aspas duplas

        // 2. Divide o texto por quebras de linha duplas (\n\n) para criar parágrafos separados no ADF
        String[] paragraphs = escapedText.split("\n\n");

        StringBuilder contentBuilder = new StringBuilder();

        for (String paragraph : paragraphs) {
            String cleanParagraph = paragraph.trim();

            if (!cleanParagraph.isEmpty()) {
                // Dentro do parágrafo, substitui quebras de linha simples restantes (\n) por \n dentro da string JSON,
                // que o Jira ADF renderiza como uma quebra de linha.
                String safeContent = cleanParagraph.replace("\n", "\\n");

                // Adiciona cada parágrafo como um bloco de 'paragraph' no ADF
                contentBuilder.append(String.format(
                        ",{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"text\": \"%s\"}]}",
                        safeContent
                ));
            }
        }

        // Remove a vírgula inicial (se houver conteúdo) e monta a estrutura final
        String finalContent = contentBuilder.length() > 0 ? contentBuilder.substring(1) : "";

        // Estrutura mínima do ADF (Atlassian Document Format)
        return String.format(
                "{\"type\": \"doc\", \"version\": 1, \"content\": [%s]}",
                finalContent.isEmpty() ? "{\"type\": \"paragraph\", \"content\": [{\"type\": \"text\", \"text\": \"Sem descrição.\"}]}" : finalContent
        );
    }


    /**
     * Envia a requisição HTTP POST para a API do Jira para criar um Issue.
     */
    private String enviarRequisicao(String jsonPayload) {
        try {
            // Cria o cabeçalho de autenticação Basic com o username e API Token
            String authString = jiraUsername + ":" + jiraApiToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

            URI uri = URI.create(jiraUrl + "/rest/api/3/issue");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("SUCESSO: Ticket do Jira criado. Status HTTP: " + statusCode);
                String[] parte01 = responseBody.split(",");
                String[] parte02 = parte01[1].split(":");

                return  parte02[1].replace("\"","").trim();
            } else {
                System.err.println("FALHA na API do Jira. Código: " + statusCode);
                System.err.println("   Resposta do Jira: " + responseBody);
                return null;
            }

        } catch (HttpTimeoutException e) {
            System.err.println("ERRO DE CONEXÃO: Tempo limite (Timeout) atingido ao tentar acessar Jira.");

            return null;
        } catch (IOException | InterruptedException e) {
            System.err.println("ERRO GERAL na requisição HTTP para o Jira: " + e.getMessage());

            return null;
        }

    }

}
