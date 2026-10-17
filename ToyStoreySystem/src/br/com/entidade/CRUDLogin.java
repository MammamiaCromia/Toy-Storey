package br.com.entidade;

import br.com.controle.Senha;

public class CRUDLogin extends DAO {

    /** Hash de uma senha qualquer: gasta o mesmo tempo quando o login não existe. */
    private static final String HASH_FALSO = Senha.gerarHash("senha-falsa");

    public static class Usuario {
        private final int id;
        private final String nome;
        private final String cargo;

        public Usuario(int id, String nome, String cargo) {
            this.id = id;
            this.nome = nome;
            this.cargo = cargo;
        }

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getCargo() { return cargo; }
    }

    /** Devolve o usuário se login e senha conferem; null caso contrário. */
    public Usuario autenticar(String login, String senha) throws Exception {
        try {
            abrirBanco("auth");     // usuário que só enxerga vw_login
            pst = con.prepareStatement(
                    "SELECT id_pessoa, nome, senha_hash, cargo FROM vw_login WHERE login = ?");
            pst.setString(1, login);
            rs = pst.executeQuery();

            if (!rs.next()) {
                Senha.verificar(senha, HASH_FALSO);
                return null;
            }
            if (!Senha.verificar(senha, rs.getString("senha_hash"))) {
                return null;
            }
            return new Usuario(rs.getInt("id_pessoa"), rs.getString("nome"), rs.getString("cargo"));
        } finally {
            fecharBanco();
        }
    }
}