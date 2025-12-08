package school.sptech.cortex.monitoramento.service;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import school.sptech.cortex.monitoramento.modelo.CapturaProcesso;
import school.sptech.cortex.monitoramento.modelo.CapturaProcessoPrincipal;

import school.sptech.cortex.monitoramento.util.CsvProcessoReader;
import school.sptech.cortex.monitoramento.util.ProcessoPrincipalWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProcessadorDeProcessos {

    public List<CapturaProcessoPrincipal> historicoProcesso(AmazonS3 s3Client, String nomeArquivo,
                                                            String bucket, String nomeProcesso, LambdaLogger logger, String trusted) throws IOException {

        // pegar o csv do bucket
        // transformar em objeto
        CsvProcessoReader leitor = new CsvProcessoReader();
        List<CapturaProcesso> listaCompleta = leitor.lerECarregarCapturasProcesso(nomeArquivo,bucket,s3Client);

        // percorrer os objetos e:


        List<LocalDateTime> dadosTimestamp = new ArrayList<>();
        List<CapturaProcessoPrincipal> listaPrincipal = new ArrayList<>();
        List<CapturaProcesso> gravarLista = new ArrayList<>();
        for (int i = 0; i < listaCompleta.size();i++){
            // guardar todos os timestamps não repetidos
            CapturaProcesso completoIndex = listaCompleta.get(i);

            LocalDateTime anterior = completoIndex.getTimestamp().plusMinutes(1);
            if(i != 0){
                anterior = listaCompleta.get(i - 1).getTimestamp();
            }

            if(!completoIndex.getTimestamp().equals(anterior)){
                dadosTimestamp.add(completoIndex.getTimestamp());
            }
            //criar objetos processo principal quando encontrar um registro e adicionar em uma lista
            String[] processo = completoIndex.getProcesso().split("\\.");

            if(processo[0].equals(nomeProcesso)){
                CapturaProcessoPrincipal novoPrincipal = new CapturaProcessoPrincipal(completoIndex.getTimestamp(),completoIndex.getCpu(),
                        completoIndex.getRam(), completoIndex.getGpu(), false, nomeProcesso);

                listaPrincipal.add(novoPrincipal);

                CapturaProcesso gravar = new CapturaProcesso(completoIndex.getFk_modelo(), completoIndex.getFk_zona(), completoIndex.getFk_empresa(),
                        completoIndex.getTimestamp(), completoIndex.getProcesso(),completoIndex.getCpu(), completoIndex.getRam(),
                        completoIndex.getDadosGravados(), completoIndex.getGpu(),completoIndex.getDiscoUso());
                gravarLista.add(gravar);
            }
        }


        if (!gravarLista.isEmpty()){
            ProcessoPrincipalWriter writer = new ProcessoPrincipalWriter();
            ByteArrayOutputStream csvGravar = writer.writeCsv(gravarLista);
            InputStream csvInputStream = new ByteArrayInputStream(csvGravar.toByteArray());
            s3Client.putObject(trusted, "processos/" + nomeArquivo, csvInputStream, null);
        }

        return listaPrincipal;

        // se o tamanho das duas listas for diferente: houve downtime
      //  if(dadosTimestamp.size() != listaPrincipal.size()){
            // comparar qual timestamp não existe na lista
            // criar registros de downtime d processo
            // adicionar na posição
         //   Boolean tem = false;
           // List<CapturaProcessoPrincipal> invisiveis = new ArrayList<>();
          //  List<Integer> index = new ArrayList<>();
         //   for (int j = 0; j < dadosTimestamp.size();j++){
          //      for(int i = 0; i < listaPrincipal.size(); i++){
          //          if(dadosTimestamp.get(j).isAfter(listaPrincipal.get(i).getTimestamp()) ||
                  //          dadosTimestamp.get(j).equals(listaPrincipal.get(i).getTimestamp())){

                  //      if(dadosTimestamp.get(j).equals(listaPrincipal.get(i).getTimestamp())){
                 //           tem = true;
                    //        break;
                     //   }

                     //   break;
                 //   }
                 //   }
                 //   if(!tem){
                 //       CapturaProcessoPrincipal capturaInvisivel =new CapturaProcessoPrincipal(dadosTimestamp.get(j),
                      //          0.0,
                     //           0.0, 0.0,
                     //           true, nomeProcesso);

                       // index.add(j);
                       // invisiveis.add(capturaInvisivel);
                 //   }
                 //   tem = false;


           // }
           // for(int m = 0; m < index.size();m++){
          //      listaPrincipal.add(index.get(m),invisiveis.get(m));
          //  }


         //   return listaPrincipal;
       // }else {
           // return listaPrincipal;
       // }
    }

}
