package org.example.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class EncryptionUtil {
    @Value("${aes.secret}")
    private String aesKey;

    private static final String ALGORITHM = "AES";

    public String encrypt(String data) {
        try {
            // Обрезаем/дополняем ключ до 32 байт (AES-256)
            byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
            byte[] fixedKey = new byte[32];
            System.arraycopy(keyBytes, 0, fixedKey, 0, Math.min(keyBytes.length, 32));
            // Дополняем нулями, если короче 32 байт
            for (int i = keyBytes.length; i < 32; i++) {
                fixedKey[i] = 0;
            }

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(fixedKey, ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            // Аналогично для decrypt
            byte[] keyBytes = aesKey.getBytes(StandardCharsets.UTF_8);
            byte[] fixedKey = new byte[32];
            System.arraycopy(keyBytes, 0, fixedKey, 0, Math.min(keyBytes.length, 32));
            for (int i = keyBytes.length; i < 32; i++) {
                fixedKey[i] = 0;
            }

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(fixedKey, ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}