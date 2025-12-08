package school.sptech.cortex.monitoramento.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import school.sptech.cortex.monitoramento.modelo.CapturaProcesso;
import school.sptech.cortex.monitoramento.modelo.CapturaSistema;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProcessoPrincipalWriter {
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    public ByteArrayOutputStream writeCsv(List<CapturaProcesso> capturas) throws IOException{

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("fk_modelo", "fk_zona", "fk_empresa",
                    "timestamp", "processo", "cpu" , "ram" , "dados_gravados" , "gpu", "disco_uso"));

        for (CapturaProcesso captura : capturas) {
            csvPrinter.printRecord(
                    captura.getFk_modelo(),
                    captura.getFk_zona(),
                    captura.getFk_empresa(),
                    captura.getTimestamp().format(FORMATADOR_TIMESTAMP),
                    captura.getProcesso(),
                    captura.getCpu(),
                    captura.getRam(),
                    captura.getDadosGravados(),
                    captura.getGpu(),
                    captura.getDiscoUso()
            );
        }

            csvPrinter.flush();
            csvPrinter.close();


        return outputStream;
    }
}
