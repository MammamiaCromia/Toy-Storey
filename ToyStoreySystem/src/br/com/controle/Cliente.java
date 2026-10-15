package br.com.controle;

/**
 *
 * @author joser
 */

import java.time.LocalDate;
import java.time.Period;

public class Cliente extends Pessoa {

    private String cpf;                 // 11 dígitos, único (uq_cliente_cpf)
    private LocalDate dataNascimento;
    private int pontosFidelidade;       // RN20: começa em 0

    public Cliente() {
        super();
        this.pontosFidelidade = 0;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public int getPontosFidelidade() {
        return pontosFidelidade;
    }

    public void setPontosFidelidade(int pontosFidelidade) {
        this.pontosFidelidade = pontosFidelidade;
    }

    public int getIdade() {
        if (dataNascimento == null) {
            return 0;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
}