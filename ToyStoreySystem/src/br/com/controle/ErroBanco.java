package br.com.controle;

import java.sql.SQLException;

public final class ErroBanco {

    public static final String MSG_SEM_PERMISSAO = "Seu perfil não tem permissão para esta operação.";

    private ErroBanco() {
    }

    /** 1044/1142/1143/1370 = acesso negado a banco, tabela, coluna ou rotina. */
    public static boolean semPermissao(SQLException ex) {
        int c = ex.getErrorCode();
        return c == 1044 || c == 1142 || c == 1143 || c == 1370;
    }
}