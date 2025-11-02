package school.sptech.cortex.monitoramento.dao;

import school.sptech.cortex.monitoramento.conexao.MySQLConnection;
import school.sptech.cortex.monitoramento.modelo.Parametro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LimiteDAO {
    private static final String SQL_SELECT_LIMITES =
            "SELECT e.id id_empresa, e.nome nome_empresa, zd.nome nome_zona, md.id_modelo, \n" +
                    "md.nome nome_modelo, ip ip_modelo, hostname hostname_modelo, \n" +
                    "tempo_parametro_min tempo_min, limite_cpu, limite_disco, \n" +
                    "limite_ram, limite_gpu FROM modelo md\n" +
                    "INNER JOIN zonadisponibilidade zd ON zd.id_zona = md.fk_zona_disponibilidade\n" +
                    "INNER JOIN empresa e ON e.id = zd.fk_empresa\n" +
                    "WHERE md.ip = ? AND md.hostname = ?";

    public Parametro buscarLimitesPorMaquina(String ip, String hostname) {
        Parametro limites = null;

        try (Connection conn = MySQLConnection.conectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_LIMITES)) {

            pstmt.setString(1, ip);
            pstmt.setString(2, hostname);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Mapeia o resultado do banco para o POJO Parametro (usando Wrappers)
                    limites = new Parametro(
                            rs.getObject("id_modelo", Integer.class),
                            rs.getString("nome_modelo"),
                            rs.getString("ip_modelo"),
                            rs.getString("hostname_modelo"),
                            rs.getObject("limite_cpu", Double.class),
                            rs.getObject("limite_ram", Double.class),
                            rs.getObject("limite_disco_uso", Double.class),
                            rs.getObject("limite_gpu", Double.class),
                            rs.getObject("tempo_min", Integer.class)
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parâmetros no DAO: " + e.getMessage());
            // Lógica de tratamento de erro (ex: logar e relançar)
        }
        return limites;
    }
}
