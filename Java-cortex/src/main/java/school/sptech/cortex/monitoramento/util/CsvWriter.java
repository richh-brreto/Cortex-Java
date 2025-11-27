package school.sptech.cortex.monitoramento.util;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import school.sptech.cortex.monitoramento.modelo.CapturaSistema;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvWriter {

    public ByteArrayOutputStream writeCsv(List<CapturaSistema> capturas) throws IOException{

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("fk_modelo", "fk_zona", "fk_empresa",
                    "timestamp", "cpu" , "ram" , "armazenamento" , "discoUso", "gpu"));

        for (CapturaSistema captura : capturas) {
            csvPrinter.printRecord(
                    captura.getFk_modelo(),
                    captura.getFk_zona(),
                    captura.getFk_empresa(),
                    captura.getTimestamp(),
                    captura.getCpu(),
                    captura.getRam(),
                    captura.getArmazenamento(),
                    captura.getDiscoUso(),
                    captura.getGpu()
            );
        }

            csvPrinter.flush();
            csvPrinter.close();


        return outputStream;
    }
}
