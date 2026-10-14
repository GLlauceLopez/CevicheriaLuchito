package com.luchito.config;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Seguridad {

    private static final int ITERACIONES = 600_000;
    private static final int LONGITUD_CLAVE = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private Seguridad() {
    }

    public static String generarHash(String clave)
            throws GeneralSecurityException {

        if (clave == null || clave.length() < 8 || clave.length() > 128) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener entre 8 y 128 caracteres.");
        }

        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);

        byte[] hash = calcular(clave, sal, ITERACIONES);

        return "pbkdf2_sha256"
                + "$" + ITERACIONES
                + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String clave, String hashGuardado)
            throws GeneralSecurityException {

        if (clave == null
                || clave.length() > 128
                || hashGuardado == null) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split("\\$", -1);

            if (partes.length != 4
                    || !"pbkdf2_sha256".equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);

            if (iteraciones < 600_000 || iteraciones > 2_000_000) {
                return false;
            }

            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);

            if (sal.length != 16 || hashEsperado.length != 32) {
                return false;
            }

            byte[] hashCalculado = calcular(clave, sal, iteraciones);

            return MessageDigest.isEqual(hashEsperado, hashCalculado);

        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    private static byte[] calcular(
            String clave,
            byte[] sal,
            int iteraciones
    ) throws GeneralSecurityException {

        char[] caracteres = clave.toCharArray();

        PBEKeySpec especificacion = new PBEKeySpec(
                caracteres,
                sal,
                iteraciones,
                LONGITUD_CLAVE
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return fabrica.generateSecret(especificacion).getEncoded();

        } finally {
            especificacion.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
