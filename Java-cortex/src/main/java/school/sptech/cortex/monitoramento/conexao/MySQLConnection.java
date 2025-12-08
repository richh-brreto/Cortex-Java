package school.sptech.cortex.monitoramento.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    private static final String URL =
    private static final String USUARIO = 
     private static final String SENHA =

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
