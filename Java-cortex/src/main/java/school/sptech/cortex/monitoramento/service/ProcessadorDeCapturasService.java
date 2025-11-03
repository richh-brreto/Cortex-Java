package school.sptech.cortex.monitoramento.service;


import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.CapturaSistema;
import school.sptech.cortex.monitoramento.modelo.Parametro;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private void checarEGerarAlerta(
            String componente,
            Double valorAtual,
            Double limiteCritico, // Recebemos apenas o limite CRÍTICO do banco
            Integer tempoBaseMinutos,
            LocalDateTime[] inicioRiscoTimestamps,
            CapturaSistema captura,
            List<Alerta> alertasGerados
    ) {
        // Validação básica
        if (limiteCritico == null || tempoBaseMinutos == null || tempoBaseMinutos <= 0) return;

        // 1. CÁLCULO DA REGRA DE NEGÓCIO
        // Limite de Atenção: Limite Crítico - 10%
        Double limiteAtencao = limiteCritico * 0.90;

        // Tempo de Atenção: Metade do tempo base
        Integer tempoAtencaoMinutos = Math.max(1, tempoBaseMinutos / 2); // Garante que seja pelo menos 1 minuto

        String tipoAlerta = null;
        LocalDateTime timestampAtual = captura.getTimestamp();

        // 2. ANÁLISE CRÍTICA (Usa Limite Crítico e Tempo Base)
        if (valorAtual >= limiteCritico) {

            // Se estiver em risco crítico, marca o tempo de início
            if (inicioRiscoTimestamps[1] == null) {
                inicioRiscoTimestamps[1] = timestampAtual;
            }

            // Verifica se o tempo mínimo CRÍTICO foi atingido
            if (inicioRiscoTimestamps[1] != null && ChronoUnit.MINUTES.between(inicioRiscoTimestamps[1], timestampAtual) >= tempoBaseMinutos) {
                tipoAlerta = "CRÍTICO";
                // Reseta o timestamp de risco crítico após gerar o alerta
                inicioRiscoTimestamps[1] = null;
            }

            // Reseta o timestamp de atenção (já é crítico)
            inicioRiscoTimestamps[0] = null;

        }
        // 3. ANÁLISE DE ATENÇÃO (Usa Limite de Atenção e Tempo de Atenção)
        else if (valorAtual >= limiteAtencao) {

            // Se estiver em risco de atenção, marca o tempo de início
            if (inicioRiscoTimestamps[0] == null) {
                inicioRiscoTimestamps[0] = timestampAtual;
            }

            // Verifica se o tempo mínimo de ATENÇÃO (metade do tempo base) foi atingido
            if (inicioRiscoTimestamps[0] != null && ChronoUnit.MINUTES.between(inicioRiscoTimestamps[0], timestampAtual) >= tempoAtencaoMinutos) {
                tipoAlerta = "ATENÇÃO";
                // Reseta o timestamp de risco de atenção após gerar o alerta
                inicioRiscoTimestamps[0] = null;
            }
            // Reseta o timestamp crítico (não é crítico)
            inicioRiscoTimestamps[1] = null;

        }
        // 4. Nível Normal
        else {
            // Se o valor voltou ao normal, reseta ambos os timestamps
            inicioRiscoTimestamps[0] = null;
            inicioRiscoTimestamps[1] = null;
        }


        // 5. Geração do Alerta
        if (tipoAlerta != null) {
            String timestampFormatado = timestampAtual.format(FORMATADOR_TIMESTAMP);

            // Define o limite usado no alerta (Crítico ou o Limite de Atenção calculado)
            Double limiteUsado = (tipoAlerta.equals("CRÍTICO") ? limiteCritico : limiteAtencao);

            Alerta novoAlerta = new Alerta(
                    tipoAlerta,
                    limitesDaMaquina.getNomeEmpresa(),
                    captura.getHostname(),
                    captura.getIp(),
                    componente,
                    valorAtual,
                    limiteUsado,
                    timestampFormatado
            );
            alertasGerados.add(novoAlerta);
            System.out.println(">>> ALERTA GERADO: " + novoAlerta);
        }
    }


    public List<Alerta> processar(List<CapturaSistema> capturas) {
        List<Alerta> alertasGerados = new ArrayList<>();

        if (limitesDaMaquina == null) {
            System.err.println("Limites não carregados. Abortando processamento.");
            return alertasGerados;
        }

        // O tempo base é o tempo necessário para o alerta CRÍTICO
        Integer tempoBaseMinutos = limitesDaMaquina.getTempoParametroMin();

        // INÍCIO DA ANÁLISE SEQUENCIAL
        for (CapturaSistema captura : capturas) {

            // --- 1. ANÁLISE DE CPU ---
            checarEGerarAlerta("CPU", captura.getCpu(), limitesDaMaquina.getLimiteCpu(), tempoBaseMinutos,
                    cpuRiscoInicio, captura, alertasGerados);

            // --- 2. ANÁLISE DE RAM ---
            checarEGerarAlerta("RAM", captura.getRam(), limitesDaMaquina.getLimiteRam(), tempoBaseMinutos,
                    ramRiscoInicio, captura, alertasGerados);

            // --- 3. ANÁLISE DE DISCO ---
            checarEGerarAlerta("Disco", captura.getDiscoUso(), limitesDaMaquina.getLimiteDiscoUso(), tempoBaseMinutos,
                    discoRiscoInicio, captura, alertasGerados);

            // --- 4. ANÁLISE DE GPU ---
            checarEGerarAlerta("GPU", captura.getGpu(), limitesDaMaquina.getLimiteGpu(), tempoBaseMinutos,
                    gpuRiscoInicio, captura, alertasGerados);
        }

        return alertasGerados;
    }
}
