/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.controle;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author joser
 */
public final class Senha {
 
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SAL = 16;        // bytes
    private static final int TAMANHO_HASH_BITS = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();
 
    private Senha() {
    }
    
        /** Gera o texto a ser gravado no banco. */
    public static String gerarHash(String senha) {
        byte[] sal = new byte[TAMANHO_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(senha.toCharArray(), sal, ITERACOES);
        return "pbkdf2$" + ITERACOES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }
 
    /** Confere uma senha digitada contra o texto gravado no banco. */
    public static boolean verificar(String senha, String armazenado) {
        if (senha == null || armazenado == null) {
            return false;
        }
        String[] partes = armazenado.split("\\$");
        if (partes.length != 4 || !partes[0].equals("pbkdf2")) {
            return false;
        }
        
        try {
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(senha.toCharArray(), sal, iteracoes);
            return MessageDigest.isEqual(esperado, calculado);   // comparação em tempo constante
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
 
    private static byte[] derivar(char[] senha, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha, sal, iteracoes, TAMANHO_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao gerar o hash da senha.", e);
        }
    }
    
}