package com.company.employee_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption/decryption for biometric face templates
 * and 1:1 cosine similarity matching. Never stores plaintext templates.
 */
@Service
public class BiometricCryptoService {

    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BIT_LENGTH = 128;

    private final SecretKeySpec secretKeySpec;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom secureRandom = new SecureRandom();

    public BiometricCryptoService(
            @Value("${app.biometric.encryption-key:SmartBiometricAES256MasterKey2026!}") String masterKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(masterKey.getBytes(StandardCharsets.UTF_8));
            this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AES-256 key for biometric templates", e);
        }
    }

    public String encryptTemplate(double[] vector) {
        try {
            String json = objectMapper.writeValueAsString(vector);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Biometric template encryption failed", e);
        }
    }

    public double[] decryptTemplate(String encryptedBase64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv));
            byte[] plainText = cipher.doFinal(cipherText);

            return objectMapper.readValue(new String(plainText, StandardCharsets.UTF_8), double[].class);
        } catch (Exception e) {
            throw new RuntimeException("Biometric template decryption failed", e);
        }
    }

    /**
     * Computes combined cosine + normalized Euclidean similarity in [0.0, 1.0].
     */
    public double calculateSimilarity(double[] enrolled, double[] probe) {
        if (enrolled == null || probe == null || enrolled.length == 0 || enrolled.length != probe.length) {
            return 0.0;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        double sqDiff = 0.0;

        for (int i = 0; i < enrolled.length; i++) {
            dot += enrolled[i] * probe[i];
            normA += enrolled[i] * enrolled[i];
            normB += probe[i] * probe[i];
            double diff = enrolled[i] - probe[i];
            sqDiff += diff * diff;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        double cosine = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        double cosineNormalized = Math.max(0.0, Math.min(1.0, (cosine + 1.0) / 2.0));
        double euclidean = Math.sqrt(sqDiff / enrolled.length);
        double euclideanScore = 1.0 / (1.0 + euclidean * 2.5);

        // Weighted score: 65% cosine direction + 35% magnitude proximity
        return Math.round((cosineNormalized * 0.65 + euclideanScore * 0.35) * 10000.0) / 10000.0;
    }
}
