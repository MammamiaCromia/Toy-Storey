package br.com.entidade;

import br.com.controle.Cliente;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class CRUDCliente extends DAO {

    private static final String SELECT_BASE
            = "SELECT p.id_pessoa, p.nome, p.status, c.cpf, c.data_nascimento, c.pontos_fidelidade "
            + "FROM cliente c INNER JOIN pessoa p ON p.id_pessoa = c.id_pessoa ";


    public int inserir(Cliente c) throws Exception {
        try {
            abrirBanco();
            con.setAutoCommit(false);   // início da transação

            int id;
            String sqlPessoa = "INSERT INTO pessoa(nome, status) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPessoa, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getNome());
                ps.setString(2, c.getStatus());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Não foi possível obter o id gerado.");
                    }
                    id = keys.getInt(1);
                }
            }

            String sqlCliente = "INSERT INTO cliente(id_pessoa, cpf, data_nascimento, pontos_fidelidade) "
                    + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlCliente)) {
                ps.setInt(1, id);
                ps.setString(2, c.getCpf());
                ps.setDate(3, java.sql.Date.valueOf(c.getDataNascimento()));
                ps.setInt(4, c.getPontosFidelidade());
                ps.executeUpdate();
            }

            con.commit();
            c.setId(id);
            return id;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            desfazer();
            throw e;
        } finally {
            finalizar();
        }
    }


    public int atualizar(Cliente c) throws Exception {
        try {
            abrirBanco();
            con.setAutoCommit(false);

            String sqlPessoa = "UPDATE pessoa SET nome = ?, status = ? WHERE id_pessoa = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlPessoa)) {
                ps.setString(1, c.getNome());
                ps.setString(2, c.getStatus());
                ps.setInt(3, c.getId());
                ps.executeUpdate();
            }

            int linhas;
            String sqlCliente = "UPDATE cliente SET cpf = ?, data_nascimento = ? WHERE id_pessoa = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlCliente)) {
                ps.setString(1, c.getCpf());
                ps.setDate(2, java.sql.Date.valueOf(c.getDataNascimento()));
                ps.setInt(3, c.getId());
                linhas = ps.executeUpdate();
            }

            if (linhas == 0) {          // id não é de cliente: nada deve ser alterado
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

    public int inativar(int id) throws Exception {
        try {
            abrirBanco();
            String query = "UPDATE pessoa SET status = 'Inativo' "
                    + "WHERE id_pessoa = ? AND id_pessoa IN (SELECT id_pessoa FROM cliente)";
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

    
    public int deletar(Cliente c) throws Exception {
        try {
            abrirBanco();
            String query = "DELETE FROM pessoa "
                    + "WHERE id_pessoa = ? AND id_pessoa IN (SELECT id_pessoa FROM cliente)";
            pst = con.prepareStatement(query);
            pst.setInt(1, c.getId());
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
    public Cliente encontrar(int id) throws Exception {
        try {
            abrirBanco();
            pst = con.prepareStatement(SELECT_BASE + "WHERE c.id_pessoa = ?");
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
    // READ (um): busca pelo CPF. Devolve null se não existir.
    // ------------------------------------------------------------------
    public Cliente encontrarPorCpf(String cpf) throws Exception {
        try {
            abrirBanco();
            pst = con.prepareStatement(SELECT_BASE + "WHERE c.cpf = ?");
            pst.setString(1, cpf);
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
    public List<Cliente> listar() throws Exception {
        List<Cliente> lista = new ArrayList<>();
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
    private Cliente montar(ResultSet r) throws SQLException {
        Cliente c = new Cliente();
        c.setId(r.getInt("id_pessoa"));
        c.setNome(r.getString("nome"));
        c.setStatus(r.getString("status"));
        c.setCpf(r.getString("cpf"));
        c.setDataNascimento(r.getDate("data_nascimento").toLocalDate());
        c.setPontosFidelidade(r.getInt("pontos_fidelidade"));
        return c;
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