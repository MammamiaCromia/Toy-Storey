package br.com.entidade;

import br.com.controle.Senha;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/** Uso único: regera o hash de senha de todos os funcionários. NÃO versionar. */
public class GeradorSenhas extends DAO {

    public static void main(String[] args) throws Exception {
        String senha = JOptionPane.showInputDialog("Senha de demonstração para todos os funcionários:");
        if (senha == null || senha.isEmpty()) {
            return;
        }

        GeradorSenhas g = new GeradorSenhas();
        g.abrirBanco("gerente");               // sem login ainda: usa o perfil direto
        try {
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement p = g.con.prepareStatement("SELECT id_pessoa FROM funcionario");
                 ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    ids.add(r.getInt(1));
                }
            }

            try (PreparedStatement p = g.con.prepareStatement(
                    "UPDATE funcionario SET senha_hash = ? WHERE id_pessoa = ?")) {
                for (int id : ids) {
                    p.setString(1, Senha.gerarHash(senha));   // sal novo para cada funcionário
                    p.setInt(2, id);
                    p.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(null, ids.size() + " senha(s) atualizada(s).");
        } finally {
            g.fecharBanco();
        }
    }
}