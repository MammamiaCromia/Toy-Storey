/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.entidade;

 
import br.com.controle.Funcionario;
import br.com.controle.Senha;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joser
 */
public class CRUDFuncionario extends DAO{
    
        private static final String SELECT_BASE
            = "SELECT p.id_pessoa, p.nome, p.status, f.login, f.cargo "
            + "FROM funcionario f INNER JOIN pessoa p ON p.id_pessoa = f.id_pessoa ";
 
    public int inserir(Funcionario f, String senha) throws Exception {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        try {
            String hash = Senha.gerarHash(senha);
 
            abrirBanco();
            con.setAutoCommit(false);
 
            int id;
            String sqlPessoa = "INSERT INTO pessoa(nome, status) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPessoa, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, f.getNome());
                ps.setString(2, f.getStatus());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Não foi possível obter o id gerado.");
                    }
                    id = keys.getInt(1);
                }
            }
 
            String sqlFuncionario = "INSERT INTO funcionario(id_pessoa, login, senha_hash, cargo) "
                + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlFuncionario)) {
                ps.setInt(1, id);
                ps.setString(2, f.getLogin());
                ps.setString(3, hash);
                ps.setString(4, f.getCargo());
                ps.executeUpdate();
            }
 
            con.commit();
            f.setId(id);
            return id;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            desfazer();
            throw e;
        } finally {
            finalizar();
        }
    }
 
    // ------------------------------------------------------------------
    // UPDATE: altera nome/status (pessoa) e login/cargo (funcionario)
    // numa transação. A senha NÃO é alterada aqui (use alterarSenha).
    // Devolve 0 se o id não for de um funcionário.
    // ------------------------------------------------------------------
    public int atualizar(Funcionario f) throws Exception {
        try {
            abrirBanco();
            con.setAutoCommit(false);
 
            String sqlPessoa = "UPDATE pessoa SET nome = ?, status = ? WHERE id_pessoa = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlPessoa)) {
                ps.setString(1, f.getNome());
                ps.setString(2, f.getStatus());
                ps.setInt(3, f.getId());
                ps.executeUpdate();
            }
 
            int linhas;
            String sqlFuncionario = "UPDATE funcionario SET login = ?, cargo = ? WHERE id_pessoa = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlFuncionario)) {
                ps.setString(1, f.getLogin());
                ps.setString(2, f.getCargo());
                ps.setInt(3, f.getId());
                linhas = ps.executeUpdate();
            }
 
            if (linhas == 0) {          // id não é de funcionário: nada deve ser alterado
                desfazer();
                return 0;
            }
            con.commit();
            return linhas;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            desfazer();
            throw e;
        } finally {
            finalizar();
        }
    }
 
    // ------------------------------------------------------------------
    // ALTERAR SENHA: grava um novo hash (com um sal novo)
    // ------------------------------------------------------------------
    public int alterarSenha(int id, String novaSenha) throws Exception {
        if (novaSenha == null || novaSenha.isEmpty()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        try {
            String hash = Senha.gerarHash(novaSenha);
            abrirBanco();
            String query = "UPDATE funcionario SET senha_hash = ? WHERE id_pessoa = ?";
            pst = con.prepareStatement(query);
            pst.setString(1, hash);
            pst.setInt(2, id);
            return pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        } finally {
            fecharBanco();
        }
    }
 
    // ------------------------------------------------------------------
    // INATIVAR: caminho recomendado pela RN05 (mantém o histórico de vendas)
    // ------------------------------------------------------------------
    public int inativar(int id) throws Exception {
        try {
            abrirBanco();
            String query = "UPDATE pessoa SET status = 'Inativo' "
                    + "WHERE id_pessoa = ? AND id_pessoa IN (SELECT id_pessoa FROM funcionario)";
            pst = con.prepareStatement(query);
            pst.setInt(1, id);
            return pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        } finally {
            fecharBanco();
        }
    }
 
    // ------------------------------------------------------------------
    // DELETE: apaga a pessoa (o ON DELETE CASCADE leva a linha de funcionario).
    // Funcionário que já registrou vendas NÃO pode ser apagado:
    // fk_venda_funcionario é RESTRICT (erro 1451).
    // ------------------------------------------------------------------
    public int deletar(Funcionario f) throws Exception {
        try {
            abrirBanco();
            String query = "DELETE FROM pessoa "
                    + "WHERE id_pessoa = ? AND id_pessoa IN (SELECT id_pessoa FROM funcionario)";
            pst = con.prepareStatement(query);
            pst.setInt(1, f.getId());
            return pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        } finally {
            fecharBanco();
        }
    }
 
    // ------------------------------------------------------------------
    // READ (um): busca pelo id. Devolve null se não existir.
    // ------------------------------------------------------------------
    public Funcionario encontrar(int id) throws Exception {
        try {
            abrirBanco();
            pst = con.prepareStatement(SELECT_BASE + "WHERE f.id_pessoa = ?");
            pst.setInt(1, id);
            rs = pst.executeQuery();
            return rs.next() ? montar(rs) : null;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        } finally {
            fecharBanco();
        }
    }
 
    // ------------------------------------------------------------------
    // READ (um): busca pelo login. Devolve null se não existir.
    // ------------------------------------------------------------------
    public Funcionario encontrarPorLogin(String login) throws Exception {
        try {
            abrirBanco();
            pst = con.prepareStatement(SELECT_BASE + "WHERE f.login = ?");
            pst.setString(1, login);
            rs = pst.executeQuery();
            return rs.next() ? montar(rs) : null;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        } finally {
            fecharBanco();
        }
    }
 
    // ------------------------------------------------------------------
    // READ (vários): lista todos por ordem de nome (para uma JTable).
    // ------------------------------------------------------------------
    public List<Funcionario> listar() throws Exception {
        List<Funcionario> lista = new ArrayList<>();
        try {
            abrirBanco();
            pst = con.prepareStatement(SELECT_BASE + "ORDER BY p.nome");
            rs = pst.executeQuery();
            while (rs.next()) {
                lista.add(montar(rs));
            }
            return lista;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        } finally {
            fecharBanco();
        }
    }
 
    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------
    private Funcionario montar(ResultSet r) throws SQLException {
        Funcionario f = new Funcionario();
        f.setId(r.getInt("id_pessoa"));
        f.setNome(r.getString("nome"));
        f.setStatus(r.getString("status"));
        f.setLogin(r.getString("login"));
        f.setCargo(r.getString("cargo"));
        return f;
    }
 
    private void desfazer() {
        try {
            if (con != null) {
                con.rollback();
            }
        } catch (SQLException ignored) {
        }
    }
 
    private void finalizar() {
        try {
            if (con != null) {
                con.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
        }
        try {
            fecharBanco();
        } catch (Exception ignored) {
        }
    }
    
}
