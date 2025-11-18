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
    public Alerta checarEGerarAlerta(
            String componente,
            Double valorAtual,
            Double limiteCritico, // Recebemos apenas o limite CRÍTICO do banco
            Integer tempoBaseMinutos,
            LocalDateTime[] inicioRiscoTimestamps,
            CapturaSistema captura
    ) {
        // Validação básica
        if (limiteCritico == null || tempoBaseMinutos == null || tempoBaseMinutos <= 0) return;

        // 1. CÁLCULO DA REGRA DE NEGÓCIO
        // Limite de Atenção: Limite Crítico - 10%
        Double limiteAtencao = limiteCritico * 0.90;

        // Tempo de Atenção: Metade do tempo base
        Integer tempoAtencaoMinutos = Math.max(1, tempoBaseMinutos / 2); // Garante que seja pelo menos 1 minuto

        String tipoAlerta = null;

        // 2. ANÁLISE CRÍTICA (Usa Limite Crítico e Tempo Base)
        if (valorAtual >= limiteCritico) {
            // LOGICA DE CRITICO CHECANDO O JSON
            // LEMBRAR DE SETAR O TIPO DE ALERTA


        }
        // 3. ANÁLISE DE ATENÇÃO (Usa Limite de Atenção e Tempo de Atenção)
        else if (valorAtual >= limiteAtencao) {

            // LOGICA DE ATENCAO CHECANDO O JSON
            // LEMBRAR DE SETAR O TIPO DE ALERTA
        }

        // 5. Geração do Alerta
        if (tipoAlerta != null) {
            String timestampFormatado = timestampAtual.format(FORMATADOR_TIMESTAMP);

            // Define o limite usado no alerta (Crítico ou o Limite de Atenção calculado)
            Double limiteUsado = (tipoAlerta.equals("CRÍTICO") ? limiteCritico : limiteAtencao);

            Alerta novoAlerta = new Alerta(
                    tipoAlerta,
                    limitesDaMaquina.getNomeEmpresa(),
                    limitesDaMaquina.getNome(),
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


}
