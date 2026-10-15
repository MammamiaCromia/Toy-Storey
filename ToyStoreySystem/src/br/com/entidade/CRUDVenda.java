package br.com.entidade;

import br.com.controle.Venda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CRUDVenda extends DAO {

    private static final Logger LOG = Logger.getLogger(CRUDVenda.class.getName());

    /** Linha do carrinho (produto + quantidade). */
    public static class ItemCarrinho {
        private final int idProduto;
        private final int quantidade;

        public ItemCarrinho(int idProduto, int quantidade) {
            this.idProduto = idProduto;
            this.quantidade = quantidade;
        }

        public int getIdProduto() { return idProduto; }
        public int getQuantidade() { return quantidade; }
    }

    // ------------------------------------------------------------------
    // Operação central (B1/B3): tudo em UMA transação
    // ------------------------------------------------------------------
    public int registrarVenda(int idFuncionario, Integer idCliente,
            List<ItemCarrinho> itens, String formaPagamento) throws Exception {
        return registrarVenda(idFuncionario, idCliente, itens, formaPagamento, false);
    }

    /**
     * simularFalha = true lança erro depois dos itens e da baixa de estoque,
     * antes do pagamento, para demonstrar o ROLLBACK.
     */
    public int registrarVenda(int idFuncionario, Integer idCliente,
            List<ItemCarrinho> itens, String formaPagamento,
            boolean simularFalha) throws Exception {

        if (itens == null || itens.isEmpty()) {
            throw new RegraNegocioException("O carrinho está vazio.");
        }

        // Soma duplicados (PK de item_venda é id_venda+id_produto) e ordena por
        // id_produto: todas as vendas travam na mesma ordem (evita deadlock 1213).
        Map<Integer, Integer> qtdPorProduto = new TreeMap<>();
        for (ItemCarrinho it : itens) {
            if (it.getQuantidade() <= 0) {
                throw new RegraNegocioException("A quantidade de cada item deve ser maior que zero.");
            }
            qtdPorProduto.merge(it.getIdProduto(), it.getQuantidade(), Integer::sum);
        }

        abrirBanco();
        try {
            con.setAutoCommit(false);                       // BEGIN

            Integer idade = (idCliente == null) ? null : buscarIdadeCliente(idCliente);
            int idVenda = inserirCabecalho(idFuncionario, idCliente);

            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<Integer, Integer> e : qtdPorProduto.entrySet()) {
                total = total.add(venderProduto(idVenda, e.getKey(), e.getValue(), idade));
            }

            if (simularFalha) {
                throw new IllegalStateException("Falha simulada no meio da transação.");
            }

            inserirPagamento(idVenda, formaPagamento, total);
            atualizarTotal(idVenda, total);
            if (idCliente != null) {
                creditarPontos(idCliente, total);
            }

            con.commit();                                   // COMMIT
            return idVenda;
        } catch (Exception e) {
            try {
                con.rollback();                             // ROLLBACK
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Falha no rollback", ex);
            }
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ex) {
                LOG.log(Level.FINE, null, ex);
            }
            fecharBanco();
        }
    }

    // ------------------------------------------------------------------
    // Consulta de uma venda pelo id
    // ------------------------------------------------------------------
    public Venda encontrar(int id) throws Exception {
        try {
            abrirBanco();
            pst = con.prepareStatement("SELECT id_venda, id_funcionario, id_cliente, data_hora, "
                    + "valor_total, status FROM venda WHERE id_venda = ?");
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (!rs.next()) {
                return null;
            }
            Venda v = new Venda();
            v.setId(rs.getInt("id_venda"));
            v.setIdFuncionario(rs.getInt("id_funcionario"));
            int cli = rs.getInt("id_cliente");
            v.setIdCliente(rs.wasNull() ? null : Integer.valueOf(cli));
            v.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
            v.setValorTotal(rs.getBigDecimal("valor_total"));
            v.setStatus(rs.getString("status"));
            return v;
        } finally {
            fecharBanco();
        }
    }

    // ------------------------------------------------------------------
    // Passos da transação (todos usam a MESMA conexão `con`)
    // ------------------------------------------------------------------
    private int buscarIdadeCliente(int idCliente) throws SQLException, RegraNegocioException {
        String sql = "SELECT TIMESTAMPDIFF(YEAR, c.data_nascimento, CURDATE()) AS idade "
                + "FROM cliente c INNER JOIN pessoa p ON p.id_pessoa = c.id_pessoa "
                + "WHERE c.id_pessoa = ? AND p.status = 'Ativo'";
        try (PreparedStatement p = con.prepareStatement(sql)) {
            p.setInt(1, idCliente);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    throw new RegraNegocioException("Cliente não encontrado ou inativo.");
                }
                return r.getInt("idade");
            }
        }
    }

    private int inserirCabecalho(int idFuncionario, Integer idCliente) throws SQLException {
        String sql = "INSERT INTO venda(id_funcionario, id_cliente, data_hora) VALUES (?, ?, NOW())";
        try (PreparedStatement p = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, idFuncionario);
            if (idCliente == null) {
                p.setNull(2, Types.INTEGER);
            } else {
                p.setInt(2, idCliente);
            }
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) {
                k.next();
                return k.getInt(1);
            }
        }
    }

    /** Trava o produto, valida, grava o item e dá baixa. Devolve o subtotal. */
    private BigDecimal venderProduto(int idVenda, int idProduto, int qtd, Integer idade)
            throws SQLException, RegraNegocioException {

        String nome, tipo, status;
        BigDecimal preco;
        int classificacao;

        String sel = "SELECT nome, preco_venda, tipo, classificacao_idade, status "
                + "FROM produto WHERE id_produto = ? FOR UPDATE";
        try (PreparedStatement p = con.prepareStatement(sel)) {
            p.setInt(1, idProduto);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    throw new RegraNegocioException("Produto " + idProduto + " não existe.");
                }
                nome = r.getString("nome");
                preco = r.getBigDecimal("preco_venda");
                tipo = r.getString("tipo");
                classificacao = r.getInt("classificacao_idade");
                status = r.getString("status");
            }
        }

        if (!"Ativo".equals(status)) {
            throw new RegraNegocioException("O produto \"" + nome + "\" está inativo.");
        }
        if (classificacao > 0) {
            if (idade == null) {
                throw new RegraNegocioException("\"" + nome + "\" tem classificação de "
                        + classificacao + " anos: identifique o cliente.");
            }
            if (idade < classificacao) {
                throw new RegraNegocioException("\"" + nome + "\" não pode ser vendido: "
                        + "classificação de " + classificacao + " anos.");
            }
        }

        String ins = "INSERT INTO item_venda(id_venda, id_produto, quantidade, preco_congelado) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement p = con.prepareStatement(ins)) {
            p.setInt(1, idVenda);
            p.setInt(2, idProduto);
            p.setInt(3, qtd);
            p.setBigDecimal(4, preco);          // preço congelado no momento da venda
            p.executeUpdate();
        }

        if ("Kit".equals(tipo)) {
            baixarKit(idProduto, qtd, nome);
        } else {
            baixarEstoque(idProduto, qtd);
        }
        return preco.multiply(BigDecimal.valueOf(qtd));
    }

    /** Kit: a baixa vai para os componentes (o estoque do kit em si não muda). */
    private void baixarKit(int idKit, int qtdKits, String nomeKit)
            throws SQLException, RegraNegocioException {
        List<int[]> componentes = new ArrayList<>();
        String sql = "SELECT id_componente, quantidade_necessaria FROM kit_composicao "
                + "WHERE id_kit = ? ORDER BY id_componente";
        try (PreparedStatement p = con.prepareStatement(sql)) {
            p.setInt(1, idKit);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    componentes.add(new int[]{r.getInt(1), r.getInt(2)});
                }
            }
        }
        if (componentes.isEmpty()) {
            throw new RegraNegocioException("O kit \"" + nomeKit + "\" não tem componentes cadastrados.");
        }
        for (int[] c : componentes) {
            baixarEstoque(c[0], qtdKits * c[1]);
        }
    }

    /** UPDATE protegido: só baixa se houver saldo. 0 linhas = estoque insuficiente. */
    private void baixarEstoque(int idProduto, int qtd) throws SQLException, RegraNegocioException {
        String sql = "UPDATE produto SET quantidade_estoque = quantidade_estoque - ? "
                + "WHERE id_produto = ? AND quantidade_estoque >= ?";
        try (PreparedStatement p = con.prepareStatement(sql)) {
            p.setInt(1, qtd);
            p.setInt(2, idProduto);
            p.setInt(3, qtd);
            if (p.executeUpdate() == 0) {
                throw new RegraNegocioException(
                        "Estoque insuficiente para \"" + nomeProduto(idProduto) + "\".");
            }
        }
    }

    private String nomeProduto(int idProduto) throws SQLException {
        try (PreparedStatement p = con.prepareStatement("SELECT nome FROM produto WHERE id_produto = ?")) {
            p.setInt(1, idProduto);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : ("id " + idProduto);
            }
        }
    }

    private void inserirPagamento(int idVenda, String forma, BigDecimal valor) throws SQLException {
        String sql = "INSERT INTO pagamento(id_venda, forma_pagamento, valor_pago) VALUES (?, ?, ?)";
        try (PreparedStatement p = con.prepareStatement(sql)) {
            p.setInt(1, idVenda);
            p.setString(2, forma);
            p.setBigDecimal(3, valor);
            p.executeUpdate();
        }
    }

    private void atualizarTotal(int idVenda, BigDecimal total) throws SQLException {
        try (PreparedStatement p = con.prepareStatement(
                "UPDATE venda SET valor_total = ? WHERE id_venda = ?")) {
            p.setBigDecimal(1, total);
            p.setInt(2, idVenda);
            p.executeUpdate();
        }
    }

    /** Suposição: 1 ponto por real gasto. Ajuste para a regra que vocês definiram. */
    private void creditarPontos(int idCliente, BigDecimal total) throws SQLException {
        int pontos = total.setScale(0, RoundingMode.DOWN).intValue();
        try (PreparedStatement p = con.prepareStatement(
                "UPDATE cliente SET pontos_fidelidade = pontos_fidelidade + ? WHERE id_pessoa = ?")) {
            p.setInt(1, pontos);
            p.setInt(2, idCliente);
            p.executeUpdate();
        }
    }
}