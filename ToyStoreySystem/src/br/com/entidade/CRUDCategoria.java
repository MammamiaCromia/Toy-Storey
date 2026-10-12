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
            String query = "INSERT INTO categoria(nome) "
            + "values(?)";
            pst=(PreparedStatement) con.prepareStatement(query);
            pst.setString(1,a.getNome());
            pst.execute();
            fecharBanco();
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
        }
    }
}