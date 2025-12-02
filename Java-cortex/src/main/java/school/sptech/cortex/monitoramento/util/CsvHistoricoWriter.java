package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.s3.AmazonS3;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import school.sptech.cortex.monitoramento.modelo.CapturaProcessoPrincipal;
import school.sptech.cortex.monitoramento.modelo.HistoricoAlerta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvHistoricoWriter {

    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public void escreverCsv(String bucket, AmazonS3 s3Client,
                            List<HistoricoAlerta> novosRegistros,
                            String nomeArquivo){

        try {
            List<HistoricoAlerta> registrosAntigos = new CsvHistoricoReader().leExibeArquivoCsv(nomeArquivo,s3Client,bucket);

            for(HistoricoAlerta h : novosRegistros){
                registrosAntigos.add(h);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("cpu", "ram", "disco",
                    "gpu", "valorCpu" , "valorRam" , "valorGpu" , "valorDisco", "timestamp","downtime_server",
                    "downtime_processo","cpu_processo","ram_processo","gpu_processo"));

            for(HistoricoAlerta h : registrosAntigos){
                csvPrinter.printRecord(
                        h.getCpu(),
                        h.getRam(),
                        h.getDisco(),
                        h.getGpu(),
                        h.getValorCpu(),
                        h.getValorRam(),
                        h.getValorGpu(),
                        h.getValorDisco(),
                        h.getTimestamp().format(FORMATADOR_TIMESTAMP),
                        h.getDowntimeServer(),
                        h.getDowntimeProcesso(),
                        h.getCpu_processo(),
                        h.getRam_processo(),
                        h.getGpu_processo()
                );
            }

            csvPrinter.flush();
            csvPrinter.close();

            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(outputStream.toByteArray());

            s3Client.putObject(bucket, "jira/" + nomeArquivo, inputStream, null);
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();

        }
    }
}
