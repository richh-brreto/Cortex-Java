package school.sptech.cortex.monitoramento.util;


import school.sptech.cortex.monitoramento.modelo.CapturaSistema;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    private static final String SEPARADOR = ","; // Alterar para ";" se necessário
    // Formato de data/hora no seu CSV: 2025-10-26_00-28-10. O padrão ISO pode não funcionar diretamente com o '_'.
    // A forma mais segura é ajustar o formato ou a string lida. Vamos usar um Formatador:
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");


    public List<CapturaSistema> lerECarregarCapturasSistema(InputStream arquivo) {
        List<CapturaSistema> capturas = new ArrayList<>();

        int numeroLinha = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(arquivo))) {

            br.readLine(); // Pula o cabeçalho
            numeroLinha++;


            String linha;
            while ((linha = br.readLine()) != null) {
                numeroLinha++;
                String[] dados = linha.split(SEPARADOR);

                // A LINHA SÓ É IGNORADA SE TIVER MENOS DE 9 COLUNAS
                if (dados.length < 9) {
                    System.err.println("Linha " + numeroLinha + ": Ignorada. Colunas insuficientes. Esperado: 9, Encontrado: " + dados.length);
                    continue;
                }

                try {
                    // 1. EXTRAÇÃO E VALIDAÇÃO (NULO/VAZIO)
                    String fk_modelo = validarString(dados[0], "fk_modelo", numeroLinha);
                    String fk_zona = validarString(dados[1], "fk_zona", numeroLinha);
                    String fk_empresa = validarString(dados[2], "fk_empresa", numeroLinha);
                    String timestampStr = validarString(dados[3], "Timestamp", numeroLinha); // String bruta do CSV

                    // 2. CONVERSÃO E VALIDAÇÃO NUMÉRICA

                    // Conversão de Timestamp (usa o formatador "yyyy-MM-dd_HH-mm-ss")
                    LocalDateTime timestamp = LocalDateTime.parse(timestampStr, FORMATADOR_TIMESTAMP);

                    // Conversão e Validação de Range [0.0, 100.0]
                    Double cpu = validarRange(dados[4], "CPU", numeroLinha);
                    Double ram = validarRange(dados[5], "RAM", numeroLinha);
                    Double armazenamento = Double.valueOf(dados[6]);
                    Double discoUso = validarRange(dados[7], "Disco Uso", numeroLinha);
                    Double gpu = validarRange(dados[8], "GPU", numeroLinha);

                    // 3. CRIAÇÃO DO OBJETO
                    CapturaSistema novaCaptura = new CapturaSistema(
                            fk_modelo, fk_zona, fk_empresa, timestamp, cpu, ram, armazenamento, discoUso, gpu
                    );
                    capturas.add(novaCaptura);

                } catch (java.time.format.DateTimeParseException e) {
                    // **NOVO CATCH PARA PEGAR ERROS DE FORMATO DE DATA/HORA**
                    System.err.println("Linha " + numeroLinha + ": Ignorada. Motivo: Formato de Timestamp inválido. Detalhe: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    // Captura: Nulo/Vazio, Fora do Range, Não-Numérico
                    System.err.println("Linha " + numeroLinha + ": Ignorada. Motivo: " + e.getMessage());
                } catch (Exception e) {
                    // Captura outros erros inesperados (ex: ArrayIndexOutOfBounds (embora o 'if' deva evitar))
                    System.err.println("Linha " + numeroLinha + ": Ignorada. Erro inesperado: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo CSV: " + e.getMessage());
        }

        return capturas;
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
