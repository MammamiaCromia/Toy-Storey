/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.controle;

/**
 *
 * @author joser
 */
public class Categoria {
    private String Nome;
    private java.math.BigDecimal Percentual;
    private int Id;

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String Nome) {
        this.Nome = Nome;
    }
    

    public java.math.BigDecimal getPercentual() {
        return Percentual;
    }

    public void setPercentual(java.math.BigDecimal percentual) {
        this.Percentual = percentual;
    }
    
}
