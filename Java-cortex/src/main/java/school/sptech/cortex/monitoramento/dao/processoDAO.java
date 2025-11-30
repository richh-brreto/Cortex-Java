package school.sptech.cortex.monitoramento.dao;

import school.sptech.cortex.monitoramento.conexao.MySQLConnection;
import school.sptech.cortex.monitoramento.modelo.Parametro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class processoDAO {
    private static final String SQL_SELECT_PROCESSO =
            "SELECT md.nome_processo FROM modelo md WHERE md.id_modelo = ?";

    public String buscarNomeProcessoPrincipal(String id_modelo) {
        String processoPrincipal = null;

        try (Connection conn = MySQLConnection.conectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_PROCESSO)) {

            pstmt.setString(1, id_modelo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Mapeia o resultado do banco para o POJO Parametro (usando Wrappers)
                    processoPrincipal = rs.getString("nome_processo");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parâmetros no DAO: " + e.getMessage());
            // Lógica de tratamento de erro (ex: logar e relançar)
        }
        return processoPrincipal;
    }
}
