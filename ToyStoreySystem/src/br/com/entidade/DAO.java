package br.com.entidade;

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

    // Lê config.properties (fora do Git). Variáveis de ambiente têm prioridade.
    private static Properties carregarConfig() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            p.load(in);
        } catch (IOException e) {
            LOG.log(Level.WARNING,
                "config.properties não encontrado; usando só variáveis de ambiente.");
        }
        return p;
    }

    private static String cfg(String chave, String envVar) {
        String v = System.getenv(envVar);
        return (v != null && !v.isEmpty()) ? v : CONFIG.getProperty(chave);
    }

    public void abrirBanco() throws SQLException {
        String url = cfg("db.url", "DB_URL");
        String user = cfg("db.user", "DB_USER");
        String senha = cfg("db.password", "DB_PASSWORD");
        if (url == null || user == null || senha == null) {
            throw new SQLException("Configuração do banco ausente (config.properties).");
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