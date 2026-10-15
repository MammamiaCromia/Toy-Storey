package br.com.entidade;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class CRUDConsulta extends DAO {
    /**
     * Consulta 1: vendas por funcionário e forma de pagamento no período.
     * JOIN de 4 tabelas + COUNT/SUM/AVG + GROUP BY.
     * forma = null significa "todas as formas".
     * Colunas: funcionário, forma, qtd, total, ticket médio.
     */
    public List<Object[]> vendasPorFuncionario(LocalDateTime inicio,
            LocalDateTime fimExclusivo, String forma) throws Exception {

        StringBuilder sql = new StringBuilder(
                "SELECT p.nome AS funcionario, pg.forma_pagamento, "
                + "COUNT(*) AS qtd_vendas, SUM(pg.valor_pago) AS total, "
                + "AVG(pg.valor_pago) AS ticket_medio "
                + "FROM venda v "
                + "INNER JOIN funcionario f ON f.id_pessoa = v.id_funcionario "
                + "INNER JOIN pessoa p ON p.id_pessoa = f.id_pessoa "
                + "INNER JOIN pagamento pg ON pg.id_venda = v.id_venda "
                + "WHERE v.status = 'Concluída' "
                + "AND v.data_hora >= ? AND v.data_hora < ? ");
        if (forma != null) {
            sql.append("AND pg.forma_pagamento = ? ");
        }
        sql.append("GROUP BY p.id_pessoa, p.nome, pg.forma_pagamento "
                + "ORDER BY total DESC");

        List<Object[]> linhas = new ArrayList<>();
        try {
            abrirBanco();
            pst = con.prepareStatement(sql.toString());
            int i = 1;
            pst.setTimestamp(i++, Timestamp.valueOf(inicio));
            pst.setTimestamp(i++, Timestamp.valueOf(fimExclusivo));
            if (forma != null) {
                pst.setString(i++, forma);
            }
            rs = pst.executeQuery();
            while (rs.next()) {
                linhas.add(new Object[]{
                    rs.getString("funcionario"),
                    rs.getString("forma_pagamento"),
                    rs.getInt("qtd_vendas"),
                    rs.getBigDecimal("total"),
                    rs.getBigDecimal("ticket_medio")
                });
            }
            return linhas;
        } finally {
            fecharBanco();
        }
    }

    /**
     * Consulta 2: produtos mais vendidos por categoria no período.
     * JOIN de 4 tabelas + SUM + GROUP BY + HAVING.
     * idCategoria = null significa "todas". minimo = qtd mínima vendida (HAVING).
     * Colunas: categoria, produto, qtd vendida, receita.
     */
    public List<Object[]> produtosMaisVendidos(LocalDateTime inicio,
            LocalDateTime fimExclusivo, Integer idCategoria, int minimo) throws Exception {

        StringBuilder sql = new StringBuilder(
                "SELECT c.nome AS categoria, pr.nome AS produto, "
                + "SUM(iv.quantidade) AS qtd_vendida, "
                + "SUM(iv.quantidade * iv.preco_congelado) AS receita "
                + "FROM item_venda iv "
                + "INNER JOIN venda v ON v.id_venda = iv.id_venda "
                + "INNER JOIN produto pr ON pr.id_produto = iv.id_produto "
                + "INNER JOIN categoria c ON c.id_categoria = pr.id_categoria "
                + "WHERE v.status = 'Concluída' "
                + "AND v.data_hora >= ? AND v.data_hora < ? ");
        if (idCategoria != null) {
            sql.append("AND c.id_categoria = ? ");
        }
        sql.append("GROUP BY c.nome, pr.id_produto, pr.nome "
                + "HAVING SUM(iv.quantidade) >= ? "
                + "ORDER BY qtd_vendida DESC");

        List<Object[]> linhas = new ArrayList<>();
        try {
            abrirBanco();
            pst = con.prepareStatement(sql.toString());
            int i = 1;
            pst.setTimestamp(i++, Timestamp.valueOf(inicio));
            pst.setTimestamp(i++, Timestamp.valueOf(fimExclusivo));
            if (idCategoria != null) {
                pst.setInt(i++, idCategoria);
            }
            pst.setInt(i++, minimo);
            rs = pst.executeQuery();
            while (rs.next()) {
                linhas.add(new Object[]{
                    rs.getString("categoria"),
                    rs.getString("produto"),
                    rs.getInt("qtd_vendida"),
                    rs.getBigDecimal("receita")
                });
            }
            return linhas;
        } finally {
            fecharBanco();
        }
    }
    
    /**
 * Relatório consolidado: lê a VIEW vw_resumo_vendas_diarias.
 * Período inclusivo nas duas pontas (a coluna dia é DATE, sem hora).
 * Colunas: dia, qtd vendas, itens vendidos, faturamento, ticket médio.
 */
    public List<Object[]> resumoDiario(LocalDate inicio, LocalDate fim) throws Exception {
        String sql = "SELECT dia, qtd_vendas, itens_vendidos, faturamento, ticket_medio "
                + "FROM vw_resumo_vendas_diarias "
                + "WHERE dia BETWEEN ? AND ? "
                + "ORDER BY dia DESC";
        List<Object[]> linhas = new ArrayList<>();
        try {
            abrirBanco();
            pst = con.prepareStatement(sql);
            pst.setDate(1, java.sql.Date.valueOf(inicio));
            pst.setDate(2, java.sql.Date.valueOf(fim));
            rs = pst.executeQuery();
            while (rs.next()) {
                linhas.add(new Object[]{
                    rs.getDate("dia").toLocalDate(),
                    rs.getInt("qtd_vendas"),
                    rs.getInt("itens_vendidos"),
                    rs.getBigDecimal("faturamento"),
                    rs.getBigDecimal("ticket_medio")
                });
            }
            return linhas;
        } finally {
        fecharBanco();
        }
    }
}