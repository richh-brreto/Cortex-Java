package school.sptech.cortex.monitoramento.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class CsvHistoricoNovoWriter {
    public ByteArrayOutputStream writeCsv() throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("cpu", "ram", "disco",
                "gpu", "valorCpu" , "valorRam" , "valorGpu" , "valorDisco", "timestamp",
                "downtime_server","downtime_processo","cpu_processo","ram_processo","gpu_processo"));


        csvPrinter.flush();
        csvPrinter.close();


        return outputStream;
    }
}
