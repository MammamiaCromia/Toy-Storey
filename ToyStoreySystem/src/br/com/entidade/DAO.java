package br.com.entidade;

import br.com.controle.Sessao;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DAO {

    private static final Logger LOG = Logger.getLogger(DAO.class.getName());
    private static final Properties CONFIG = carregarConfig();

    protected Connection con;
    protected PreparedStatement pst;
    protected ResultSet rs;

    private static Properties carregarConfig() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            p.load(in);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "config.properties não encontrado; usando só variáveis de ambiente.");
        }
        return p;
    }

    private static String cfg(String chave, String envVar) {
        String v = System.getenv(envVar);
        return (v != null && !v.isEmpty()) ? v : CONFIG.getProperty(chave);
    }

    /** Abre a conexão com o usuário MySQL do perfil de quem fez login. */
    public void abrirBanco() throws SQLException {
        String perfil = Sessao.getPerfilBanco();
        if (perfil == null) {
            throw new SQLException("Nenhum usuário autenticado.");
        }
        abrirBanco(perfil);
        // Identifica o funcionário do app para os gatilhos de auditoria (B6)
        try (PreparedStatement p = con.prepareStatement("SET @id_funcionario_app = ?")) {
            p.setInt(1, Sessao.getIdFuncionario());
            p.execute();
        }
    }

    /** Abre a conexão com um perfil específico: gerente, operador, leitura ou auth. */
    public void abrirBanco(String perfil) throws SQLException {
        String url = cfg("db.url", "DB_URL");
        String user = cfg("db." + perfil + ".user", "DB_" + perfil.toUpperCase() + "_USER");
        String senha = cfg("db." + perfil + ".password", "DB_" + perfil.toUpperCase() + "_PASSWORD");
        if (url == null || user == null || senha == null) {
            throw new SQLException("Configuração do banco ausente para o perfil " + perfil + ".");
        }
        con = DriverManager.getConnection(url, user, senha);
    }

    public void fecharBanco() {
        try { if (rs != null)  rs.close();  } catch (SQLException e) { LOG.log(Level.FINE, null, e); }
        try { if (pst != null) pst.close(); } catch (SQLException e) { LOG.log(Level.FINE, null, e); }
        try { if (con != null) con.close(); } catch (SQLException e) { LOG.log(Level.FINE, null, e); }
        rs = null; pst = null; con = null;
    }
}