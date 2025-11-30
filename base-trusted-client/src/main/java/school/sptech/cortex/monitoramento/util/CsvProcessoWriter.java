package school.sptech.cortex.monitoramento.util;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import school.sptech.cortex.monitoramento.modelo.CapturaProcesso;

import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

public class CsvProcessoWriter {
    private static final char SEPARADOR = ',';

    // ⭐️ NOVOS NOMES DE COLUNAS (10 COLUNAS)
    private static final String[] COLUNAS = new String[]{
            "ip", "hostname", "timestamp", "nomeProcesso", "cpu", "ram",
            "dadosGravados", "mac", "gpu", "discoUsoGb" // discoUsoGb, já convertido
    };

    public boolean exportarCapturasProcesso(List<CapturaProcesso> listaDeCapturas, String caminhoArquivo) {

        try (Writer writer = new FileWriter(caminhoArquivo)) {

            // 1. Cria o CSVWriter (o utilitário de baixo nível do OpenCSV)
            CSVWriter csvWriter = new CSVWriter(writer, SEPARADOR, CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            // 2. ESCRITA MANUAL DO CABEÇALHO (A SOLUÇÃO À PROVA DE FALHAS)
            csvWriter.writeNext(COLUNAS);

            // 3. Configura a Estratégia de Mapeamento
            ColumnPositionMappingStrategy<CapturaProcesso> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(CapturaProcesso.class);
            strategy.setColumnMapping(COLUNAS);

            // 4. Constrói o conversor de Objeto para CSV
            StatefulBeanToCsv<CapturaProcesso> beanToCsv = new StatefulBeanToCsvBuilder<CapturaProcesso>(csvWriter)
                    .withMappingStrategy(strategy)
                    .withSeparator(SEPARADOR)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            // 5. Escreve a lista completa no arquivo
            beanToCsv.write(listaDeCapturas);

            System.out.println("Arquivo CSV de Processos '" + caminhoArquivo + "' criado com sucesso com " + listaDeCapturas.size() + " registros tratados (Bytes->GB convertidos).");
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao exportar o arquivo CSV de Processos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}