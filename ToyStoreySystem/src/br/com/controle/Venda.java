package br.com.controle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Venda {

    private int id;
    private int idFuncionario;
    private Integer idCliente;          // opcional: venda sem cliente identificado
    private LocalDateTime dataHora;
    private BigDecimal valorTotal;
    private String status;              // "Concluída" ou "Cancelada"

    public Venda() {
        this.valorTotal = BigDecimal.ZERO;
        this.status = "Concluída";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdFuncionario() { return idFuncionario; }
    public void setIdFuncionario(int idFuncionario) { this.idFuncionario = idFuncionario; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}