package br.com.controle;

public final class Sessao {

    private static boolean logado;
    private static int idFuncionario;
    private static String nome;
    private static String cargo;

    private Sessao() {
    }

    public static void iniciar(int id, String nomeUsuario, String cargoUsuario) {
        idFuncionario = id;
        nome = nomeUsuario;
        cargo = cargoUsuario;
        logado = true;
    }

    public static void encerrar() {
        logado = false;
        idFuncionario = 0;
        nome = null;
        cargo = null;
    }

    public static boolean isLogado() { return logado; }
    public static int getIdFuncionario() { return idFuncionario; }
    public static String getNome() { return nome; }
    public static String getCargo() { return cargo; }
    public static boolean isGerente() { return logado && "Gerente".equals(cargo); }

    /** Qual usuário MySQL o DAO deve usar. null = ninguém logado. */
    public static String getPerfilBanco() {
        if (!logado) {
            return null;
        }
        return "Gerente".equals(cargo) ? "gerente" : "operador";
    }
}