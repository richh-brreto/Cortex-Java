package school.sptech.cortex.monitoramento.util;


import com.amazonaws.services.lambda.runtime.LambdaLogger;
import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.JiraIssueFields;
import school.sptech.cortex.monitoramento.modelo.JiraIssueGet;
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
import java.util.Map;
import java.util.List;


public class JiraConcatenar {
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    // VARIÁVEIS DE CONFIGURAÇÃO
    private final String jiraUrl;
    private final String jiraUsername;
    private final String jiraApiToken;


    private final HttpClient httpClient;

    public JiraConcatenar() {
        // Leitura das configurações
       this.jiraUrl =
         this.jiraUsername =
          this.jiraApiToken =


        // Verificações de configuração obrigatória
        Objects.requireNonNull(jiraUrl, "JIRA_URL não pode ser nulo.");
        Objects.requireNonNull(jiraUsername, "JIRA_USERNAME não pode ser nulo.");
        Objects.requireNonNull(jiraApiToken, "JIRA_API_TOKEN não pode ser nulo.");



        // Configuração do Cliente HTTP com timeout de 10 segundos
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }


    public void concatenarTicketsCriticos(Alerta alertas, String idJira) {

        System.out.println("--- Iniciando verificação de alertas CRÍTICOS para o Jira ---");

        // 1. FILTRAGEM

        try {
            // GET
            String json = metodoGetJira(idJira);


            MapperJiraIssue mapperJson = new MapperJiraIssue();
            JiraIssueGet fieldsGeral = mapperJson.map(json);

            JiraIssueFields fieldsEspec = fieldsGeral.getFields();


                // setando as adições
                String timestamp = alertas.getTimestamp().format(FORMATADOR_TIMESTAMP);

                String summary = String.format("ALERTA CRÍTICO: Modelo %s Máquina %s - Última Atualização %s",
                        alertas.getNomeModelo(), alertas.getHostname(),timestamp);

                String description = String.format(
                        "- %s  - com o uso de: %.2f%% - em: %s",
                        alertas.getTipoMetrica(), alertas.getValorAtual(),
                        timestamp
                );

                String categoria = alertas.getTipoMetrica().toLowerCase();

                // concatenando a string de descrição
                String descAntiga = adfToPlainText(fieldsEspec.getDescription());
                String descricaoConcatenada = descAntiga + "\n\n" + description;

                // concatenando as labels

            List<String> labels = new ArrayList<>(fieldsEspec.getLabels());
            labels.add(categoria);

          // 3. Monta o JSON de Payload
            String jsonPayload = buildIssueJson(summary, descricaoConcatenada, labels);

            // 4. Envia a requisição
            Boolean keyJira = enviarRequisicao(jsonPayload, idJira);







        } catch (Exception e) {
            System.err.println("ERRO ao processar Alerta para o Jira ");

        }

    }

    // --- MÉTODOS PRIVADOS DE SUPORTE ---


    private String buildIssueJson(String summary, String descriptionDetails, List<String> categoria) {
        String safeSummary = summary.replace("\"", "\\\"");
        String adfDescription = createAdfDescription(descriptionDetails);


        List<String> quotedLabels = new ArrayList<>();
        for (String c : categoria) {

            quotedLabels.add(String.format("\"%s\"", c));
        }


        String categoriaFormatado = String.join(",", quotedLabels);


        String jsonTemplate =
                "{" +
                        "\"fields\": {" +
                        "\"summary\": \"%s\"," +
                        "\"description\": %s," +
                        "\"labels\": [%s]" +
                        "}" +
                        "}";

        return String.format(
                jsonTemplate,
                safeSummary,
                adfDescription,
                categoriaFormatado
        );
    }
    public static String adfToPlainText(Map<String, Object> adf) {
        StringBuilder sb = new StringBuilder();

        if (adf == null || !adf.containsKey("content")) return null;

        List<Map<String, Object>> contentList = (List<Map<String, Object>>) adf.get("content");

        for (Map<String, Object> block : contentList) {
            if ("paragraph".equals(block.get("type")) && block.containsKey("content")) {
                List<Map<String, Object>> texts = (List<Map<String, Object>>) block.get("content");
                for (Map<String, Object> textNode : texts) {
                    if ("text".equals(textNode.get("type")) && textNode.containsKey("text")) {
                        sb.append(textNode.get("text"));
                    }
                }
                sb.append("\n"); // nova linha após cada parágrafo
            }
        }

        return sb.toString().trim();
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



    private Boolean enviarRequisicao(String jsonPayload, String idJira) {
        try {
            // Cria o cabeçalho de autenticação Basic com o username e API Token
            String authString = jiraUsername + ":" + jiraApiToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

            URI uri = URI.create(jiraUrl + "/rest/api/3/issue/" + idJira);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("SUCESSO: Ticket do Jira criado. Status HTTP: " + statusCode);

                return  true;
            } else {
                System.err.println("FALHA na API do Jira. Código: " + statusCode);
                System.err.println("   Resposta do Jira: " + responseBody);
                return false;
            }

        } catch (HttpTimeoutException e) {
            System.err.println("ERRO DE CONEXÃO: Tempo limite (Timeout) atingido ao tentar acessar Jira.");
            return null;
        } catch (IOException | InterruptedException e) {
            System.err.println("ERRO GERAL na requisição HTTP para o Jira: " + e.getMessage());
            return null;
        }

    }


    public String metodoGetJira(String idJira){
        try {

            String authString = jiraUsername + ":" + jiraApiToken;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
            // GET /rest/api/3/issue/{issueIdOrKey}?fields=summary,description,labels

            URI uri = URI.create(jiraUrl + "/rest/api/3/issue/" + idJira + "?fields=summary,description,labels");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization", authHeader)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode >= 200 && statusCode < 300) {
                System.out.println("SUCESSO: Ticket do Jira criado. Status HTTP: " + statusCode);

                return  responseBody;
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

    public void mudar(String idJira, LambdaLogger logger) throws IOException, InterruptedException {

        String userPass = jiraUsername + ":" + jiraApiToken;
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
        String estadoNormal = "Normal (ticket antigo)"; // Valor alvo


        String jsonPayload = String.format(
                "{\"fields\": {\"" + "customfield_10060" + "\": {\"value\": \"%s\"}}}",
                estadoNormal
        );


        String updateUrl = jiraUrl + "/rest/api/3/issue/" + idJira;

        // Requisição PUT para atualizar o campo
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(updateUrl))
                .header("Authorization", authorizationHeader)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Envia a requisição usando o cliente da classe (this.httpClient)
        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // O Jira geralmente retorna 204 No Content em caso de sucesso na atualização
        if (response.statusCode() == 204) {
            logger.log(" Campo customizado atualizado para 'Normal' no ticket " + idJira);

        } else {
            logger.log("Falha ao atualizar o ticket " + idJira);
            logger.log("Status: " + response.statusCode());
            logger.log("Corpo da Resposta: " + response.body());

        }
    }
}
