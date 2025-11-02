package school.sptech.cortex.monitoramento.conexao;

import school.sptech.cortex.monitoramento.util.ConfiguracaoAmbiente;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    private static final String URL = ConfiguracaoAmbiente.get("DB_URL");
    private static final String USUARIO = ConfiguracaoAmbiente.get("DB_USER");
    private static final String SENHA = ConfiguracaoAmbiente.get("DB_PASSWORD");

    public static Connection conectar() throws SQLException {
        try {
            return DriverManager.getConnection(URL,USUARIO,SENHA);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar no banco de dados." + e.getMessage());
            throw e;
        }
    }

    public static void fecharConexao(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão." + e.getMessage());
            }

        }
    }

}
