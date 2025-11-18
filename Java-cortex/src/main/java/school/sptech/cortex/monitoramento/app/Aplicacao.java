package school.sptech.cortex.monitoramento.app;


import school.sptech.cortex.monitoramento.dao.LimiteDAO;
import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.CapturaProcesso;
import school.sptech.cortex.monitoramento.modelo.CapturaSistema;
import school.sptech.cortex.monitoramento.modelo.Parametro;
import school.sptech.cortex.monitoramento.service.ProcessadorDeCapturasService;
import school.sptech.cortex.monitoramento.util.*;

import java.util.List;

public class Aplicacao {
    // Caminho do arquivo CSV que contém as capturas de dados da máquina
    private static final String ARQUIVO_CSV = ConfiguracaoAmbiente.get("CAMINHO_CSV");
    private static final String ARQUIVO_CSV_EXPORT = ConfiguracaoAmbiente.get("CAMINHO_CSV_EXPORT");
    private static final String ARQUIVO_CSV_PROCESSO = ConfiguracaoAmbiente.get("CAMINHO_CSV_PROCESSO");
    private static final String ARQUIVO_CSV_EXPORT_PROCESSO = ConfiguracaoAmbiente.get("CAMINHO_CSV_EXPORT_PROCESSO");

    // Caminhos dos arquivos de suporte
    private static String JSON_ALERTA_PROVAVEL_CPU = "C:/Users/User/Documents/Faculdade/2ºsemestre/Grupo PI/S3/trusted/1;1;1;alerta_verificacao_cpu.json";
    private static String JSON_ALERTA_PROVAVEL_RAM = "C:/Users/User/Documents/Faculdade/2ºsemestre/Grupo PI/S3/trusted/1;1;1;alerta_verificacao_ram.json";
    private static String JSON_ALERTA_PROVAVEL_DISCO = "C:/Users/User/Documents/Faculdade/2ºsemestre/Grupo PI/S3/trusted/1;1;1;alerta_verificacao_disco.json";
    private static String JSON_ALERTA_PROVAVEL_GPU = "C:/Users/User/Documents/Faculdade/2ºsemestre/Grupo PI/S3/trusted/1;1;1;alerta_verificacao_gpu.json";
    private static String CSV_HISTORICO = "C:/Users/User/Documents/Faculdade/2ºsemestre/Grupo PI/S3/trusted/1;1;1;CTX_41.csv";
    private static String JSON_ESTADO_ATUAL = "C:/Users/User/Documents/Faculdade/2ºsemestre/Grupo PI/S3/trusted/1;1;1.json";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   CORTEX - INICIANDO PROCESSO DE MONITORAMENTO  ");
        System.out.println("=================================================");

        if (ARQUIVO_CSV == null || ARQUIVO_CSV.isEmpty()) {
            System.err.println("ERRO: Variável de ambiente 'CAMINHO_CSV' não configurada ou vazia.");
            return;
        }


        // 1. LEITURA E CARREGAMENTO DOS DADOS DO CSV
        System.out.printf("\n[1] Lendo capturas do arquivo: %s%n", ARQUIVO_CSV);
        List<CapturaSistema> capturas = new CsvReader().lerECarregarCapturasSistema(ARQUIVO_CSV);

        if (capturas.isEmpty()) {
            System.out.println("Nenhuma captura válida encontrada. Finalizando aplicação.");
            return;
        }

        if (capturas != null) {
            System.out.printf("-> Exportando capturas para o arquivo de saída: %s%n", ARQUIVO_CSV_EXPORT);
            CsvWriter csvWriter = new CsvWriter();
            csvWriter.exportarCapturasSistema(capturas, ARQUIVO_CSV_EXPORT);
        }

        System.out.printf("\n[1] Lendo capturas do arquivo: %s%n", ARQUIVO_CSV);
        List<CapturaProcesso> capturaProcessos = new CsvProcessoReader().lerECarregarCapturasProcesso(ARQUIVO_CSV_PROCESSO);

        if (capturas.isEmpty()) {
            System.out.println("Nenhuma captura válida encontrada. Finalizando aplicação.");
            return;
        }

        if (capturas != null) {
            System.out.printf("-> Exportando capturas para o arquivo de saída: %s%n", ARQUIVO_CSV_EXPORT);
            CsvProcessoWriter csvProcessoWriter = new CsvProcessoWriter();
            csvProcessoWriter.exportarCapturasProcesso(capturaProcessos, ARQUIVO_CSV_EXPORT_PROCESSO);
        }

        // Pega os dados da primeira captura para buscar os limites no banco
        CapturaSistema primeiraCaptura = capturas.get(0);
        String id_modelo = primeiraCaptura.getFk_modelo();

        // 2. BUSCA DE LIMITES NO BANCO DE DADOS
        System.out.printf("[2] Buscando limites de parâmetros para a máquina %s (%s)...%n", id_modelo);
        LimiteDAO limiteDAO = new LimiteDAO();
        Parametro limitesDaMaquina = limiteDAO.buscarLimitesPorMaquina(id_modelo);

        if (limitesDaMaquina == null) {
            System.err.println("ERRO: Não foram encontrados limites de parâmetros para esta máquina no banco de dados.");
            return;
        }
        System.out.printf("Limites carregados com sucesso. Tempo de persistência (CRÍTICO): %d minutos.%n", limitesDaMaquina.getTempoParametroMin());

        // 3. PROCESSAMENTO DAS CAPTURAS E GERAÇÃO DE ALERTAS
        System.out.println("[3] Processando capturas e aplicando a regra de negócio...");
        ProcessadorDeCapturasService processador = new ProcessadorDeCapturasService(limitesDaMaquina);
        System.out.printf("Processamento concluído. Total de %d alertas gerados.%n");


        if (!alertasGerados.isEmpty()) {
            // 4. ENVIO DE NOTIFICAÇÕES (SLACK E JIRA)

            // --- 4.1. NOTIFICAÇÃO SLACK (Envia TODOS os tipos de alerta) ---
            System.out.println("\n[4.1] Enviando alertas para o Slack...");
            SlackNotifier slackNotifier = new SlackNotifier();
            slackNotifier.enviarAlertas(alertasGerados);


            // --- 4.2. CRIAÇÃO DE TICKETS JIRA (Envia APENAS alertas CRÍTICOS) ---
            System.out.println("\n[4.2] Verificando e criando tickets CRÍTICOS no Jira...");
            JiraTicketCreator jiraCreator = new JiraTicketCreator();
            jiraCreator.criarTicketsCriticos(alertasGerados);
        } else {
            System.out.println("\n[4] Nenhuma notificação necessária. A máquina está dentro dos limites.");
        }

        System.out.println("\n=================================================");
        System.out.println("   CORTEX - PROCESSO DE MONITORAMENTO FINALIZADO ");
        System.out.println("=================================================");
    }
}