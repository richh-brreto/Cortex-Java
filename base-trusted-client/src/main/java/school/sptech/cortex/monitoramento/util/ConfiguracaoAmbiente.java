package school.sptech.cortex.monitoramento.util;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfiguracaoAmbiente {
    private static final Dotenv DOTENV = Dotenv.load();

    private ConfiguracaoAmbiente() {}

    public static String get(String key) {
        return DOTENV.get(key);
    }
}
