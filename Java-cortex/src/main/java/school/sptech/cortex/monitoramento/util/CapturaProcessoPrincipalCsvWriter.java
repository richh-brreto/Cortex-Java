package school.sptech.cortex.monitoramento.util;

import school.sptech.cortex.monitoramento.modelo.CapturaProcessoPrincipal;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CapturaProcessoPrincipalCsvWriter {

    // Define o formato de data/hora usado no CSV
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Define o separador padrão do CSV
    private static final String SEPARADOR = ",";

    /**
     * Escreve uma lista de CapturaProcessoPrincipal em um arquivo CSV,
     * excluindo o campo 'downtime'.
     *
     * @param capturas A lista de objetos a serem gravados.
     * @param nomeArquivo O caminho completo ou nome do arquivo CSV a ser criado.
     */
    public static void escreverCsvSemDowntime(
            List<CapturaProcessoPrincipal> capturas,
            String nomeArquivo)
    {
        // Campos que serão incluídos no CSV, NA ORDEM desejada:
        String header = "timestamp,cpu,ram,gpu,nome";

        try (
                FileWriter fileWriter = new FileWriter(nomeArquivo);
                PrintWriter writer = new PrintWriter(fileWriter)
        ) {
            // 1. Escreve o cabeçalho (Header)
            writer.println(header);

            // 2. Escreve os dados, ignorando o atributo 'downtime'
            for (CapturaProcessoPrincipal captura : capturas) {
                // Formata o timestamp primeiro
                String timestampFormatado = captura.getTimestamp()
                        .format(FORMATADOR_TIMESTAMP);

                // Constrói a linha de dados, concatenando os atributos desejados
                String linha = String.join(SEPARADOR,
                        timestampFormatado,
                        String.valueOf(captura.getCpu()),
                        String.valueOf(captura.getRam()),
                        String.valueOf(captura.getGpu()),
                        // ⚠️ Omitindo captura.getDowntime()
                        captura.getNome()
                );

                writer.println(linha);
            }

            System.out.println("Arquivo CSV criado com sucesso em: " + nomeArquivo);

        } catch (IOException e) {
            System.err.println("Erro ao escrever o arquivo CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}