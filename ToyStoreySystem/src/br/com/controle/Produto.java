package br.com.controle;

import java.math.BigDecimal;

/**
 *
 * @author joser
 */
public class Produto {
 
    private int id;
    private int idCategoria;
    private String nomeCategoria;        // só leitura: vem do JOIN com categoria, não é gravado
    private String codigoBarras;         // 13 dígitos, único (uq_produto_codigo_barras)
    private String nome;
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;       // precisa ser maior que o custo (ck_produto_precos)
    private int quantidadeEstoque;       // nunca negativo (ck_produto_estoque, RN01)
    private int estoqueMinimo;
    private int classificacaoIdade;      // idade mínima recomendada (RN06)
    private String status;               // "Ativo" ou "Inativo"
    private String tipo;                 // "Simples" ou "Kit"
 
    public Produto() {
        this.status = "Ativo";
        this.tipo = "Simples";
        this.precoCusto = BigDecimal.ZERO;
        this.precoVenda = BigDecimal.ZERO;
    }
 
    public int getId() {
        return id;
    }
 
    public void setId(int id) {
        this.id = id;
    }
 
    public int getIdCategoria() {
        return idCategoria;
    }
 
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }
 
    public String getNomeCategoria() {
        return nomeCategoria;
    }
 
    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }
 
    public String getCodigoBarras() {
        return codigoBarras;
    }
 
    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }
 
    public String getNome() {
        return nome;
    }
 
    public void setNome(String nome) {
        this.nome = nome;
    }
 
    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }
 
    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }
 
    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }
 
    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }
 
    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }
 
    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }
 
    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }
 
    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }
 
    public int getClassificacaoIdade() {
        return classificacaoIdade;
    }
 
    public void setClassificacaoIdade(int classificacaoIdade) {
        this.classificacaoIdade = classificacaoIdade;
    }
 
    public String getStatus() {
        return status;
    }
 
    public void setStatus(String status) {
        this.status = status;
    }
 
    public String getTipo() {
        return tipo;
    }
 
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
 
    @Override
    public String toString() {
        return nome;   // útil para JComboBox/JList
    }
}
