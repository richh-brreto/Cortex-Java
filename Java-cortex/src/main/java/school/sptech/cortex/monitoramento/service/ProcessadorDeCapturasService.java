package school.sptech.cortex.monitoramento.service;


import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.modelo.CapturaSistema;
import school.sptech.cortex.monitoramento.modelo.Parametro;
import school.sptech.cortex.monitoramento.util.AlertaProvavelMapper;
import school.sptech.cortex.monitoramento.util.AlertaProvavelWriter;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProcessadorDeCapturasService {
    private final Parametro limitesDaMaquina;
    private static final DateTimeFormatter FORMATADOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // VARIÁVEIS DE ESTADO (Wrapper: Pode ser null quando não há risco)
    // [0] = Atenção (Tempo Base / 2), [1] = Crítico (Tempo Base)
    private LocalDateTime[] cpuRiscoInicio = {null, null};
    private LocalDateTime[] ramRiscoInicio = {null, null};
    private LocalDateTime[] discoRiscoInicio = {null, null};
    private LocalDateTime[] gpuRiscoInicio = {null, null};


    public ProcessadorDeCapturasService(Parametro limitesDaMaquina) {

        this.limitesDaMaquina = limitesDaMaquina;
    }

    // --- LÓGICA DE ALERTA (Método Auxiliar Genérico) ---
    public Alerta checarEGerarAlerta(
            String componente,
            Double valorAtual,
            Double limiteCritico, // Recebemos apenas o limite CRÍTICO do banco
            Integer tempoBaseMinutos,
            LocalDateTime timestamp,
            String nomeArquivo
    ) {
        // Validação básica
        if (limiteCritico == null || tempoBaseMinutos == null || tempoBaseMinutos <= 0) return null;

        // 1. CÁLCULO DA REGRA DE NEGÓCIO
        // Limite de Atenção: Limite Crítico - 10%
        Double limiteAtencao = limiteCritico * 0.90;

        // Tempo de Atenção: Metade do tempo base
        Integer tempoAtencaoMinutos = Math.max(1, tempoBaseMinutos / 2); // Garante que seja pelo menos 1 minuto


        // 2. ANÁLISE CRÍTICA (Usa Limite Crítico e Tempo Base)
        if (valorAtual >= limiteCritico) {
            // LOGICA DE CRITICO CHECANDO O JSON
            // LEMBRAR DE SETAR O TIPO DE ALERTA
            return checarTempo(nomeArquivo,valorAtual,timestamp,tempoBaseMinutos,"Critico");
            // 3. ANÁLISE DE ATENÇÃO (Usa Limite de Atenção e Tempo de Atenção)
        }else if (valorAtual >= limiteAtencao) {

            // LOGICA DE ATENCAO CHECANDO O JSON
            // LEMBRAR DE SETAR O TIPO DE ALERTA
            return checarTempo(nomeArquivo,valorAtual,timestamp,tempoAtencaoMinutos,"Atencao");
        }

        // 5. Geração do Alerta
        return null;
    }

    public AlertaProvavel criarAlertaProvavel(Double medida, LocalDateTime timestamp){
        AlertaProvavel alertaProvavel = new AlertaProvavel();

        alertaProvavel.setMedida(medida);
        alertaProvavel.setTimestamp(timestamp);

        return  alertaProvavel;
    }

        public Alerta criarNovoAlerta(String tipo){

    }

    public Alerta checarTempo(String nomeArquivo, Double valorAtual, LocalDateTime timestamp, Integer tempoBaseMinutos, String tipo){
            Boolean arquivoRecemCriado = false;
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(nomeArquivo);
            } catch (FileNotFoundException erro) {
                System.out.println("Arquivo não encontrado. Criando arquivo...");
                try( FileWriter writerJson = new FileWriter(nomeArquivo)) {
                    writerJson.write("[]");
                    writerJson.flush();
                    arquivoRecemCriado = true;
                } catch (IOException erro2) {
                    System.out.println("Erro ao criar arquivo Json");
                    erro2.printStackTrace();
                    System.exit(1);
                }

            }

            AlertaProvavelMapper mapper = new AlertaProvavelMapper();
            File arquivo = new File(nomeArquivo);
            List<AlertaProvavel> listaAlertasProvaveis = new ArrayList<>();

            if (arquivoRecemCriado) {

                listaAlertasProvaveis.add(criarAlertaProvavel(valorAtual, timestamp));
                // gravar json e sair da função
                AlertaProvavelWriter.escreverAlertasProvaveis(arquivo, listaAlertasProvaveis);

                return null;
            }

            try {
                listaAlertasProvaveis = mapper.mapearAlertasProvaveis(inputStream);
            }catch (IOException erro){
                System.out.println("Erro ao mapear json");
                erro.printStackTrace();
            }finally {
                try {
                    inputStream.close();
                }catch (IOException erro){
                    System.out.println("Erro ao fechar aqruivo json");
                }
            }

            LocalDateTime ultimoTimestamp = listaAlertasProvaveis.get(listaAlertasProvaveis.size() - 1).getTimestamp();

            if (ultimoTimestamp.plusSeconds(20).isAfter(timestamp)){
                // diferença maior que 20 segundos
                AlertaProvavel novoAlertaProvavel = criarAlertaProvavel(valorAtual, timestamp);
                listaAlertasProvaveis.clear();
                listaAlertasProvaveis.add(novoAlertaProvavel);

            }else{
                // diferença menor que 20 segundos
                AlertaProvavel novoAlertaProvavel = criarAlertaProvavel(valorAtual, timestamp);
                listaAlertasProvaveis.add(novoAlertaProvavel);
            }

            AlertaProvavelWriter.escreverAlertasProvaveis(arquivo, listaAlertasProvaveis);
            ultimoTimestamp = listaAlertasProvaveis.get(listaAlertasProvaveis.size() - 1).getTimestamp();
            LocalDateTime primeiroTimestamp = listaAlertasProvaveis.get(0).getTimestamp();

            if(primeiroTimestamp.plusMinutes(tempoBaseMinutos).isAfter(ultimoTimestamp)){
                // estoura o limite
                return criarNovoAlerta(tipo);
            }else {
                // não estoura o limite
                AlertaProvavelWriter.escreverAlertasProvaveis(arquivo,listaAlertasProvaveis);
                return null;
            }

        }

}
