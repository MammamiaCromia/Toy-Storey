package br.com.entidade;
import br.com.controle.Categoria;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.sql.ResultSet;

public class CRUDCategoria extends DAO{
    public void inserir(Categoria a) throws Exception {
        try {
            abrirBanco();
            String query = "INSERT INTO categoria(nome, percentual_reajuste_padrao) "
                + "values(?, ?)";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, a.getNome());
            if (a.getPercentual() == null) {
                pst.setNull(2, java.sql.Types.DECIMAL);   // campo opcional na tabela
            } else {
                pst.setBigDecimal(2, a.getPercentual());
            }
            pst.execute();
            fecharBanco();
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        }
    }

    public int atualizar(Categoria a) throws Exception {
        try {
            abrirBanco();
            String query = "UPDATE categoria SET nome = ? WHERE id_categoria = ?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setString(1, a.getNome());
            pst.setInt(2, a.getId());
            int linhas = pst.executeUpdate();
            fecharBanco();
            return linhas;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        }
    }

    public int excluir(Categoria a) throws Exception {
        try {
            abrirBanco();
            String query = "DELETE FROM categoria WHERE id_categoria = ?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, a.getId());
            int linhas = pst.executeUpdate();
            fecharBanco();
            return linhas;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        }
    }

    public Categoria encontrar(int id) throws Exception {
        try {
            abrirBanco();
            String query = "SELECT id_categoria, nome, percentual_reajuste_padrao "
                + "FROM categoria WHERE id_categoria = ?";
            pst = (PreparedStatement) con.prepareStatement(query);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            Categoria c = null;
            if (rs.next()) {
                c = new Categoria();
                c.setId(rs.getInt("id_categoria"));
                c.setNome(rs.getString("nome"));
                c.setPercentual(rs.getBigDecimal("percentual_reajuste_padrao"));
            }
            fecharBanco();
            return c;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        }
    }
}