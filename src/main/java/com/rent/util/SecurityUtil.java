package com.rent.util;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashSecret(String secret, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);

            KeySpec spec = new PBEKeySpec(
                    secret.toCharArray(),
                    saltBytes,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to hash secret", e);
        }
    }

    public static boolean verifySecret(String inputSecret,
                                       String storedHash,
                                       String storedSalt) {

        if (inputSecret == null
                || storedHash == null
                || storedSalt == null
                || inputSecret.isBlank()
                || storedHash.isBlank()
                || storedSalt.isBlank()) {
            return false;
        }

        String inputHash = hashSecret(inputSecret, storedSalt);

        return inputHash.equals(storedHash);
    }
}