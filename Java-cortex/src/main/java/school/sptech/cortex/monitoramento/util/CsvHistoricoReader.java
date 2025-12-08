package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import school.sptech.cortex.monitoramento.modelo.HistoricoAlerta;

import java.io.*;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CsvHistoricoReader {

    private static final String SEPARADOR = ","; // Alterar para ";" se necessário
    // Formato de data/hora no seu CSV: 2025-10-26_00-28-10. O padrão ISO pode não funcionar diretamente com o '_'.
    // A forma mais segura é ajustar o formato ou a string lida. Vamos usar um Formatador:
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");


    public List<HistoricoAlerta> leExibeArquivoCsv(String nomeArquivo,
                                                   AmazonS3 s3Client,
                                                   String trusted, LambdaLogger logger
                                                  ) {
        List<HistoricoAlerta> historico = new ArrayList<>();

        try (InputStream s3InputStream = s3Client.getObject(trusted, "jira/"+ nomeArquivo).getObjectContent();
             BufferedReader br = new BufferedReader(new InputStreamReader(s3InputStream))){
              // colocar o nome do csv
                logger.log("Lendo arquivo");
                br.readLine(); // Pula o cabeçalho


                String linha;
                while ((linha = br.readLine()) != null) {

                    String[] dados = linha.split(SEPARADOR);

                    Boolean cpu = Boolean.parseBoolean(dados[0]);
                    Boolean ram = Boolean.parseBoolean(dados[1]);
                    Boolean gpu = Boolean.parseBoolean(dados[2]);
                    Boolean disco = Boolean.parseBoolean(dados[3]);
                    Double valorCpu = Double.parseDouble(dados[4]);
                    Double valorGpu = Double.parseDouble(dados[5]);
                    Double valorDisco = Double.parseDouble(dados[6]);
                    Double valorRam = Double.parseDouble(dados[7]);

                    LocalDateTime timestamp = LocalDateTime.parse(dados[8], FORMATADOR_TIMESTAMP);

                    Boolean downtimeServer = Boolean.parseBoolean(dados[9]);
                    Boolean downtimeProcesso = Boolean.parseBoolean(dados[10]);
                    Double cpuProcesso = Double.parseDouble(dados[11]);
                    Double ramProcesso = Double.parseDouble(dados[12]);
                    Double gpuProcesso = Double.parseDouble(dados[13]);

                    HistoricoAlerta novoHistorico = new HistoricoAlerta(cpu, ram, gpu, disco, valorCpu, valorGpu, valorDisco, valorRam, timestamp,
                            downtimeServer,downtimeProcesso,cpuProcesso,ramProcesso,gpuProcesso);

                    historico.add(novoHistorico);


                }
                logger.log("fim do while");
                return historico;


        }catch (Exception e3){


            boolean isUnrecoverableS3Error = (e3 instanceof AmazonS3Exception) && ((AmazonS3Exception) e3).getStatusCode() != 404;
            if (isUnrecoverableS3Error) {
                logger.log("ERRO IRRECUPERÁVEL DE S3 (não 404): " + e3.getMessage());
                throw new RuntimeException("Falha de S3 ao ler Historico.", e3);
            }
            logger.log("Arquivo Historico não encontrado (404). Criando novo buffer...");


            try {
                CsvHistoricoNovoWriter escritor = new CsvHistoricoNovoWriter();
                ByteArrayOutputStream novoCsv = escritor.writeCsv();
                InputStream csvInputStream = new ByteArrayInputStream(novoCsv.toByteArray());




                s3Client.putObject(trusted,"jira/" + nomeArquivo, csvInputStream, null);

                csvInputStream.close();

                logger.log("CSV histórico criado e enviado para S3: " + nomeArquivo);
                return historico;
            } catch (Exception erro) {
                throw new RuntimeException("Erro ao criar arquivo");
            }
        }

    }
}
