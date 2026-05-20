package com.rent.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public final class DbKeyCryptoUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int DB_KEY_BYTES = 32;   // 256-bit
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int AES_BITS = 256;
    private static final int GCM_TAG_BITS = 128;

    private static final int WRAP_ITERATIONS = 210_000;

    private DbKeyCryptoUtil() {}

    public static String generateDatabaseKey() {
        byte[] key = new byte[DB_KEY_BYTES];
        RANDOM.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String encryptDatabaseKey(String plainDbKey, String password, String base64Salt) {
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);

            SecretKey secretKey = deriveAesKey(password, base64Salt);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] encrypted = cipher.doFinal(plainDbKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] output = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, output, 0, iv.length);
            System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(output);

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt database key.", e);
        }
    }

    public static String decryptDatabaseKey(String encryptedDbKey, String password, String base64Salt) {
        try {
            byte[] input = Base64.getDecoder().decode(encryptedDbKey);

            byte[] iv = new byte[IV_BYTES];
            byte[] encrypted = new byte[input.length - IV_BYTES];

            System.arraycopy(input, 0, iv, 0, IV_BYTES);
            System.arraycopy(input, IV_BYTES, encrypted, 0, encrypted.length);

            SecretKey secretKey = deriveAesKey(password, base64Salt);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] plain = cipher.doFinal(encrypted);
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt database key.", e);
        }
    }

    private static SecretKey deriveAesKey(String password, String base64Salt) throws Exception {
        byte[] salt = Base64.getDecoder().decode(base64Salt);

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, WRAP_ITERATIONS, AES_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }
}