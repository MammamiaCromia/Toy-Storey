/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.entidade;

import br.com.controle.Produto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joser
 */
public class CRUDProduto extends DAO {
 
    /** Item de combo (id + nome) para escolher a categoria na tela. */
    public static class CategoriaItem {
 
        private final int id;
        private final String nome;
 
        public CategoriaItem(int id, String nome) {
            this.id = id;
            this.nome = nome;
        }
 
        public int getId() {
            return id;
        }
 
        public String getNome() {
            return nome;
        }
 
        @Override
        public String toString() {
            return nome;   // é o que o JComboBox exibe
        }
    }
 
    private static final String SELECT_BASE
            = "SELECT p.id_produto, p.id_categoria, c.nome AS nome_categoria, p.codigo_barras, p.nome, "
            + "p.preco_custo, p.preco_venda, p.quantidade_estoque, p.estoque_minimo, "
            + "p.classificacao_idade, p.status, p.tipo "
            + "FROM produto p INNER JOIN categoria c ON c.id_categoria = p.id_categoria ";
 
    // ------------------------------------------------------------------
    // CREATE: insere e devolve o id gerado (AUTO_INCREMENT)
    // ------------------------------------------------------------------
    public int inserir(Produto p) throws Exception {
        try {
            abrirBanco();
            String query = "INSERT INTO produto(id_categoria, codigo_barras, nome, preco_custo, "
                    + "preco_venda, quantidade_estoque, estoque_minimo, classificacao_idade, status, tipo) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, p.getIdCategoria());
            pst.setString(2, p.getCodigoBarras());
            pst.setString(3, p.getNome());
            pst.setBigDecimal(4, p.getPrecoCusto());
            pst.setBigDecimal(5, p.getPrecoVenda());
            pst.setInt(6, p.getQuantidadeEstoque());
            pst.setInt(7, p.getEstoqueMinimo());
            pst.setInt(8, p.getClassificacaoIdade());
            pst.setString(9, p.getStatus());
            pst.setString(10, p.getTipo());
            pst.executeUpdate();
 
            rs = pst.getGeneratedKeys();
            int id = 0;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            p.setId(id);
            return id;
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;   // faz o erro chegar até a tela
        } finally {
            fecharBanco();
        }
    }
 
    // ------------------------------------------------------------------
    // UPDATE: altera todos os campos, localizando pelo id.
    // Devolve o número de linhas alteradas (0 = id não existe).
    // ------------------------------------------------------------------
    public int atualizar(Produto p) throws Exception {
        try {
            abrirBanco();
            String query = "UPDATE produto SET id_categoria = ?, codigo_barras = ?, nome = ?, "
                    + "preco_custo = ?, preco_venda = ?, quantidade_estoque = ?, estoque_minimo = ?, "
                    + "classificacao_idade = ?, status = ?, tipo = ? WHERE id_produto = ?";
            pst = con.prepareStatement(query);
            pst.setInt(1, p.getIdCategoria());
            pst.setString(2, p.getCodigoBarras());
            pst.setString(3, p.getNome());
            pst.setBigDecimal(4, p.getPrecoCusto());
            pst.setBigDecimal(5, p.getPrecoVenda());
            pst.setInt(6, p.getQuantidadeEstoque());
            pst.setInt(7, p.getEstoqueMinimo());
            pst.setInt(8, p.getClassificacaoIdade());
            pst.setString(9, p.getStatus());
            pst.setString(10, p.getTipo());
            pst.setInt(11, p.getId());
            return pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erro " + e.getMessage());
            throw e;
        } finally {
            fecharBanco();
        }
    }
 
    // ------------------------------------------------------------------
    // INATIVAR: caminho recomendado pela RN05 (produto vendido não se apaga)
    // ------------------------------------------------------------------
    public int inativar(int id) throws Exception {
        try {
            abrirBanco();
            String query = "UPDATE produto SET status = 'Inativo' WHERE id_produto = ?";
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
    // DELETE: apaga pelo id.
    // Produto já vendido (item_venda) ou usado como componente de kit
    // não pode ser apagado: as FKs são RESTRICT (erro 1451).
    // ------------------------------------------------------------------
    public int deletar(Produto p) throws Exception {
        try {
            abrirBanco();
            String query = "DELETE FROM produto WHERE id_produto = ?";
            pst = con.prepareStatement(query);
            pst.setInt(1, p.getId());
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
    public Produto encontrar(int id) throws Exception {
        try {
            abrirBanco();
            pst = con.prepareStatement(SELECT_BASE + "WHERE p.id_produto = ?");
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
    // READ (um): busca pelo código de barras. Devolve null se não existir.
    // ------------------------------------------------------------------
    public Produto encontrarPorCodigoBarras(String codigo) throws Exception {
        try {
            abrirBanco();
            pst = con.prepareStatement(SELECT_BASE + "WHERE p.codigo_barras = ?");
            pst.setString(1, codigo);
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
    public List<Produto> listar() throws Exception {
        List<Produto> lista = new ArrayList<>();
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
    // Categorias para o combo da tela
    // ------------------------------------------------------------------
    public List<CategoriaItem> listarCategorias() throws Exception {
        List<CategoriaItem> lista = new ArrayList<>();
        try {
            abrirBanco();
            pst = con.prepareStatement("SELECT id_categoria, nome FROM categoria ORDER BY nome");
            rs = pst.executeQuery();
            while (rs.next()) {
                lista.add(new CategoriaItem(rs.getInt("id_categoria"), rs.getString("nome")));
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
    // Auxiliar
    // ------------------------------------------------------------------
    private Produto montar(ResultSet r) throws SQLException {
        Produto p = new Produto();
        p.setId(r.getInt("id_produto"));
        p.setIdCategoria(r.getInt("id_categoria"));
        p.setNomeCategoria(r.getString("nome_categoria"));
        p.setCodigoBarras(r.getString("codigo_barras"));
        p.setNome(r.getString("nome"));
        p.setPrecoCusto(r.getBigDecimal("preco_custo"));
        p.setPrecoVenda(r.getBigDecimal("preco_venda"));
        p.setQuantidadeEstoque(r.getInt("quantidade_estoque"));
        p.setEstoqueMinimo(r.getInt("estoque_minimo"));
        p.setClassificacaoIdade(r.getInt("classificacao_idade"));
        p.setStatus(r.getString("status"));
        p.setTipo(r.getString("tipo"));
        return p;
    }
}