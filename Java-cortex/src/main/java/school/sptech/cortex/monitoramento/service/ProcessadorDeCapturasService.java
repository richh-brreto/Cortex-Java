package school.sptech.cortex.monitoramento.service;


import com.amazonaws.services.s3.AmazonS3;
import school.sptech.cortex.monitoramento.modelo.Alerta;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;
import school.sptech.cortex.monitoramento.util.AlertaProvavelWriter;
import school.sptech.cortex.monitoramento.util.MapperAlertaProvavel;


import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProcessadorDeCapturasService {


    public Alerta checarEGerarAlerta(
            String componente,
            Double valorAtual,
            Double limiteCritico, // Recebemos apenas o limite CRÍTICO do banco
            Integer tempoBaseMinutos,
            LocalDateTime timestamp,
            AmazonS3 s3Client,
            String trusted,
            String fk_modelo,
            String fk_zona,
            String fk_empresa,
            String hostname,
            String nomeModelo


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
            return checarTempo(valorAtual,timestamp,tempoBaseMinutos,"Critico", s3Client,trusted, fk_modelo,componente,fk_empresa,fk_zona,nomeModelo, hostname);
            // 3. ANÁLISE DE ATENÇÃO (Usa Limite de Atenção e Tempo de Atenção)
        }else if (valorAtual >= limiteAtencao) {

            // LOGICA DE ATENCAO CHECANDO O JSON
            // LEMBRAR DE SETAR O TIPO DE ALERTA
            return checarTempo(valorAtual,timestamp,tempoAtencaoMinutos,"Atencao", s3Client,trusted, fk_modelo,componente,fk_empresa,fk_zona,nomeModelo, hostname);
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

    public Alerta checarTempo(
                              Double valorAtual,
                              LocalDateTime timestamp,
                              Integer tempoBaseMinutos,
                              String tipo,
                              AmazonS3 s3Client,
                              String trusted,
                              String fk_modelo,
                              String componente,
                              String fk_empresa,
                              String fk_zona,
                              String nomeModelo,
                              String hostname) {

        String jsonPossibilidade = fk_modelo + ";" + componente + ";" + tipo + ";Possibilidade.json";

        List<AlertaProvavel> listaAlertasProvaveis = new ArrayList<>();

        try {
            InputStream arquivoPossibilidade = s3Client.getObject(trusted, "arquivos_apoio/"+jsonPossibilidade).getObjectContent();

            MapperAlertaProvavel mapperAlertaProvavel= new MapperAlertaProvavel();

            listaAlertasProvaveis = mapperAlertaProvavel.map(arquivoPossibilidade);

            Integer tamanho = listaAlertasProvaveis.size();
            LocalDateTime ultimoTimestamp = listaAlertasProvaveis.get(tamanho - 1).getTimestamp();

            if (ultimoTimestamp.plusSeconds(20).isAfter(timestamp)) {

                listaAlertasProvaveis.clear();
                listaAlertasProvaveis.add(criarAlertaProvavel(valorAtual, timestamp));
                AlertaProvavelWriter.escreverAlertasProvaveis(jsonPossibilidade, listaAlertasProvaveis, trusted, s3Client);

                return null;

            } else {
                //faz parte, adicionar nova captura, verifica tempo total - verifica se ultrapassa limite

                    AlertaProvavelWriter.escreverAlertasProvaveis(jsonPossibilidade, listaAlertasProvaveis, trusted, s3Client);


                ultimoTimestamp = listaAlertasProvaveis.get(tamanho - 1).getTimestamp();
                LocalDateTime primeiro = listaAlertasProvaveis.get(0).getTimestamp();

                if (primeiro.plusMinutes(tempoBaseMinutos).isAfter(ultimoTimestamp)) {
                    // ultrapassa
                    Alerta novoalerta = new Alerta(tipo, fk_modelo, fk_zona, fk_empresa, nomeModelo, hostname, componente ,valorAtual, timestamp);
                    return novoalerta;
                } else {
                    // n ultrapassa
                    return null;
                }
            }
        } catch (Exception e) {
            System.out.println("Arquivo não encontrado, criando...");
            try {
                listaAlertasProvaveis.add(criarAlertaProvavel(valorAtual, timestamp));
                AlertaProvavelWriter.escreverAlertasProvaveis(jsonPossibilidade,listaAlertasProvaveis , trusted, s3Client);

                return null;

            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }

        }

    }
}
