package school.sptech.cortex.monitoramento.util;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import school.sptech.cortex.monitoramento.modelo.CapturaSistema;

import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

public class CsvWriter {
    private static final char SEPARADOR = ','; // Caractere separador

    // Os nomes das colunas, na mesma ordem das anotações @CsvBindByPosition
    private static final String[] COLUNAS = new String[]{
            "ip", "hostname", "timestamp", "cpu", "ram",
            "armazenamento", "discoUso", "mac", "gpu"
    };

    public boolean exportarCapturasSistema(List<CapturaSistema> listaDeCapturas, String caminhoArquivo) {

        try (Writer writer = new FileWriter(caminhoArquivo)) {

            // 1. Cria o CSVWriter (o utilitário de baixo nível do OpenCSV)
            CSVWriter csvWriter = new CSVWriter(writer, SEPARADOR, CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            // 2. ESCRITA MANUAL DO CABEÇALHO (AGORA GARANTIDO!)
            csvWriter.writeNext(COLUNAS);

            // 3. Configura a Estratégia de Mapeamento (AGORA SEM CABEÇALHO, SÓ PARA MAPEAR OS DADOS)
            ColumnPositionMappingStrategy<CapturaSistema> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(CapturaSistema.class);
            strategy.setColumnMapping(COLUNAS);

            // 4. Constrói o conversor de Objeto para CSV (QUE NÃO ESCREVERÁ O CABEÇALHO, POIS JÁ FOI ESCRITO)
            StatefulBeanToCsv<CapturaSistema> beanToCsv = new StatefulBeanToCsvBuilder<CapturaSistema>(csvWriter)
                    .withMappingStrategy(strategy)
                    .withSeparator(SEPARADOR)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            // 5. Escreve a lista completa no arquivo
            beanToCsv.write(listaDeCapturas);

            System.out.println("Arquivo CSV '" + caminhoArquivo + "' criado com sucesso com " + listaDeCapturas.size() + " registros tratados (Com cabeçalho manual).");
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao exportar o arquivo CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}