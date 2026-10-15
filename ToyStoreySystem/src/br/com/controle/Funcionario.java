package br.com.controle;

/**
 *
 * @author joser
 */
public class Funcionario extends Pessoa{
 
    private String login;   // único (uq_funcionario_login)
    private String cargo;   // "Operador" ou "Gerente" (ck_funcionario_cargo)
 
    public Funcionario() {
        super();
        this.cargo = "Operador";
    }
 
    public String getLogin() {
        return login;
    }
 
    public void setLogin(String login) {
        this.login = login;
    }
 
    public String getCargo() {
        return cargo;
    }
 
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

}
