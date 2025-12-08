package school.sptech.cortex.monitoramento.util;

import com.amazonaws.services.s3.AmazonS3;
import school.sptech.cortex.monitoramento.modelo.CapturaProcesso;

import java.io.*;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvProcessoReader {
    private static final String SEPARADOR = ",";
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final double BYTES_PARA_GB = 1073741824.0;

    public List<CapturaProcesso> lerECarregarCapturasProcesso(String caminhoArquivo, String trusted, AmazonS3 s3Client) {
        List<CapturaProcesso> capturas = new ArrayList<>();
        String linha;
        int numeroLinha = 0;
        final int COLUNAS_ESPERADAS = 10;

        String chaveDecodificadora = null;
        try {
            chaveDecodificadora = URLDecoder.decode(caminhoArquivo, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        InputStream s3InputStream = s3Client.getObject(trusted, "processos/"+chaveDecodificadora).getObjectContent();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(s3InputStream))) {
            br.readLine(); // Pula o cabeçalho
            numeroLinha++;

            while ((linha = br.readLine()) != null) {
                numeroLinha++;
                String[] dados = linha.split(SEPARADOR);

                if (dados.length < COLUNAS_ESPERADAS) {
                    System.err.println("Linha " + numeroLinha + ": Ignorada. Colunas insuficientes. Esperado: " + COLUNAS_ESPERADAS + ", Encontrado: " + dados.length);
                    continue;
                }

                try {
                    // 1. EXTRAÇÃO E VALIDAÇÃO BÁSICA
                    String fk_modelo = validarString(dados[0], "fk_modelo", numeroLinha);
                    String fk_zona = validarString(dados[1], "fk_zona", numeroLinha);
                    String fk_empresa = validarString(dados[2], "fk_empresa", numeroLinha);
                    String timestampStr = validarString(dados[3], "Timestamp", numeroLinha);
                    String nomeProcesso = validarString(dados[4], "Nome Processo", numeroLinha);


                    // 2. CONVERSÃO E VALIDAÇÃO NUMÉRICA

                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr, FORMATADOR_TIMESTAMP);
                    Double cpu = validarRange(dados[5], "CPU", numeroLinha); // Reutiliza 0-100%
                    Double ram = validarRange(dados[6], "RAM", numeroLinha); // Reutiliza 0-100%

                    // Novo campo: Dados Gravados (Não há validação de range 0-100% aqui, apenas conversão para Double)
                    Double dadosGravados = Double.valueOf(validarString(dados[7], "Dados Gravados", numeroLinha));

                    Double gpu = validarRange(dados[8], "GPU", numeroLinha); // Reutiliza 0-100%

                    Double discoUsoGb = validarEConverterBytesParaGB(dados[9], "Disco Uso (Bytes)", numeroLinha);

                    // 3. CRIAÇÃO DO OBJETO
                    CapturaProcesso novaCaptura = new CapturaProcesso(
                            fk_modelo ,fk_zona, fk_empresa, timestamp, nomeProcesso, cpu, ram, dadosGravados, gpu, discoUsoGb
                    );
                    capturas.add(novaCaptura);

                } catch (Exception e) {
                    System.err.println("Linha " + numeroLinha + ": Ignorada. Erro: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo CSV: " + e.getMessage());
        }

        return capturas;
    }

    private Double validarEConverterBytesParaGB(String valorStr, String nomeCampo, int linha) throws IllegalArgumentException {
        // 1. Validação de nulo/vazio (reutilizando a lógica)
        String valorLimpo = validarString(valorStr, nomeCampo, linha);

        // 2. Conversão para Double (o valor pode ser muito grande, mas Double suporta)
        Double valorBytes;
        try {
            valorBytes = Double.valueOf(valorLimpo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo '" + nomeCampo + "' não é um número válido: " + valorLimpo);
        }

        // 3. Conversão
        Double valorGB = (valorBytes / BYTES_PARA_GB);

        valorGB = Math.round(valorGB * 10.0) / 10.0;

        // 4. Se precisar de uma validação extra de faixa para o valor em GB, adicione aqui (ex: valorGB > 1000)

        return valorGB;
    }

    private String validarString(String valor, String nomeCampo, int linha) throws IllegalArgumentException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo '" + nomeCampo + "' está vazio/nulo.");
        }
        return valor.trim();
    }

    private Double validarRange(String valorStr, String nomeCampo, int linha) throws IllegalArgumentException {
        // Validação de nulo/vazio
        String valorLimpo = validarString(valorStr, nomeCampo, linha);

        Double valor;
        try {
            valor = Double.valueOf(valorLimpo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O campo '" + nomeCampo + "' não é um número válido: " + valorLimpo);
        }

        // Validação de Range [0.0, 100.0]
        if (valor < 0.0 || valor > 100.0) {
            throw new IllegalArgumentException("O campo '" + nomeCampo + "' está fora do range [0.0, 100.0]: " + valor);
        }

        return valor;
    }
}
