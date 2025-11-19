package school.sptech.cortex.monitoramento.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import school.sptech.cortex.monitoramento.modelo.AlertaProvavel;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AlertaProvavelWriter {

    public static void escreverAlertasProvaveis(File nomeArquivo, List<AlertaProvavel> alertasProaveis) {
        ObjectMapper writerJson = new ObjectMapper();


        try{
            writerJson.writerWithDefaultPrettyPrinter().writeValue(nomeArquivo, alertasProaveis);
        }catch (IOException erro){
            System.out.println("Erro ao escrever arquivo");
            System.exit(1);
        }
    }

}
