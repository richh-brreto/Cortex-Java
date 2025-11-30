package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.s3.AmazonS3;
import school.sptech.cortex.monitoramento.modelo.HistoricoAlerta;

import java.io.*;
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
                                                   String trusted
                                                  ) {
        List<HistoricoAlerta> historico = new ArrayList<>();

        try {
              // colocar o nome do csv

            InputStream s3InputStream = s3Client.getObject(trusted, nomeArquivo).getObjectContent();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(s3InputStream))) {

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
                return historico;
            } catch (NoSuchElementException erro) {
                System.out.println("Arquivo com problemas!");
                erro.printStackTrace();

            } catch (IllegalStateException | IOException erro) {
                System.out.println("Erro na leitura do arquivo!");
                erro.printStackTrace();
            }

        }catch (Exception e3){
            try{

                CsvHistoricoNovoWriter escritor = new CsvHistoricoNovoWriter();
                ByteArrayOutputStream novoCsv = escritor.writeCsv();
                InputStream csvInputStream = new ByteArrayInputStream(novoCsv.toByteArray());
                s3Client.putObject(trusted, nomeArquivo, csvInputStream, null);

            }catch (Exception erro){
                System.out.println("Erro ao escrever arquivo");
                erro.printStackTrace();

                System.exit(1);
            }
        }
        return historico;
    }
}
