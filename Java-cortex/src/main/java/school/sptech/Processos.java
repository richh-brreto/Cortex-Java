package school.sptech;

import java.util.ArrayList;
import java.util.Scanner;

public class Processos {

    public static void adiconarLog(ArrayList<LogProcessos> lista, LogProcessos log) {
        lista.add(log);
    }

    public static void ordenarLogPorPid(ArrayList<LogProcessos> logProcessoss) {

        for (int i = 0; i < logProcessoss.size() - 1; i++) {
            int iMenor = i;
            for (int j = i + 1; j < logProcessoss.size(); j++) {

                if (logProcessoss.get(j).getPid() < logProcessoss.get(iMenor).getPid()) {
                    iMenor = j;
                }
            }

            LogProcessos aux = logProcessoss.get(i);
            logProcessoss.set(i, logProcessoss.get(iMenor));
            logProcessoss.set(iMenor, aux);

        }
    }

    public static void ordenarLogPorCpu(ArrayList<LogProcessos> logProcessoss) {

        for (int i = 0; i < logProcessoss.size() - 1; i++) {
            int iMenor = i;
            for (int j = i + 1; j < logProcessoss.size(); j++) {

                if (logProcessoss.get(j).getCpu() > logProcessoss.get(iMenor).getCpu()) {
                    iMenor = j;
                }
            }

            LogProcessos aux = logProcessoss.get(i);
            logProcessoss.set(i, logProcessoss.get(iMenor));
            logProcessoss.set(iMenor, aux);

        }
    }

    public static void ordenarLogPorRam(ArrayList<LogProcessos> logProcessoss) {

        for (int i = 0; i < logProcessoss.size() - 1; i++) {
            int iMenor = i;
            for (int j = i + 1; j < logProcessoss.size(); j++) {

                if (logProcessoss.get(j).getRam() > logProcessoss.get(iMenor).getRam()) {
                    iMenor = j;
                }
            }

            LogProcessos aux = logProcessoss.get(i);
            logProcessoss.set(i, logProcessoss.get(iMenor));
            logProcessoss.set(iMenor, aux);

        }
    }

    public static void main(String[] args) {

        Scanner leitor = new Scanner(System.in);
        ArrayList<LogProcessos> lista = new ArrayList<>();

        LogProcessos log1 = new LogProcessos("05-09-25 14:20:50", "svchost.exe", 1068, 0.0, 0.02345);
        LogProcessos log2 = new LogProcessos("05-09-25 14:21:51", "WUDFHost.exe", 1069, 0.0, 0.02345);
        LogProcessos log5 = new LogProcessos("05-09-25 14:25:10", "chrome.exe", 1065, 0.2, 0.03456);
        LogProcessos log8 = new LogProcessos("05-09-25 14:28:40", "discord.exe", 1068, 1.5, 0.05612);
        LogProcessos log3 = new LogProcessos("05-09-25 14:23:00", "explorer.exe", 1063, 0.1, 0.03012);
        LogProcessos log10 = new LogProcessos("05-09-25 14:30:15", "idea64.exe", 1070, 3.2, 0.12045);
        LogProcessos log7 = new LogProcessos("05-09-25 14:27:20", "Teams.exe", 1067, 2.1, 0.08934);
        LogProcessos log4 = new LogProcessos("05-09-25 14:24:05", "svchost.exe", 1064, 0.3, 0.02876);
        LogProcessos log6 = new LogProcessos("05-09-25 14:26:00", "spotify.exe", 1066, 0.8, 0.06789);
        LogProcessos log9 = new LogProcessos("05-09-25 14:29:05", "steam.exe", 1062, 1.9, 0.09876);

        adiconarLog(lista, log4);
        adiconarLog(lista, log5);
        adiconarLog(lista, log6);
        adiconarLog(lista, log7);
        adiconarLog(lista, log2);
        adiconarLog(lista, log3);
        adiconarLog(lista, log1);
        adiconarLog(lista, log4);
        adiconarLog(lista, log5);
        adiconarLog(lista, log9);
        adiconarLog(lista, log10);
        adiconarLog(lista, log8);

        System.out.println(lista);

        while (true) {
            System.out.println("Como deseja ordenar os logs? (cpu / ram / pid)");
            String ordenar = leitor.next().toLowerCase();

            if (ordenar.equals("cpu")) {
                ordenarLogPorCpu(lista);
                System.out.println(lista);
            } else if (ordenar.equals("ram")) {
                ordenarLogPorRam(lista);
                System.out.println(lista);
            } else if (ordenar.equals("pid")) {
                ordenarLogPorPid(lista);
                System.out.println(lista);
            } else {
                System.out.println(lista);
            }

            System.out.println("Deseja ordenar por outro método? (sim / não)");
            String repetir = leitor.next().toLowerCase();

            while (!repetir.equals("nao") || !repetir.equals("não") || !repetir.equals("sim")) {
                if (repetir.equals("não") || repetir.equals("nao")) {
                    break;
                } else if (repetir.equals("sim")) {
                    break;
                } else {
                    System.out.println("Opção inválida");
                    System.out.println("Deseja ordenar por outro método? (sim / não)");
                    repetir = leitor.next().toLowerCase();
                }
            }

            if (repetir.equals("não") || repetir.equals("nao")) {
                break;
            }
        }
    }
}