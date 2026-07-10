package com.codingagent.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class CredentialManager {
    private final String filePath;

    public CredentialManager(String filePath) {
        this.filePath = filePath;
    }

    /**
     * KeychainAdapter interface — reserved for system Keychain extension.
     * Currently uses Base64 + XOR obfuscated file storage; implementing this interface
     * can connect to macOS Keychain / Windows Credential Manager / Linux Secret Service.
     */
    public interface KeychainAdapter {
        void store(String service, String key);
        String load(String service);
        void clear(String service);
        boolean isAvailable();
    }

    /**
     * Stores a key by obfuscating it with XOR (0x5A) + Base64 encoding and writing to file.
     */
    public void store(String key) {
        try {
            byte[] keyBytes = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] xorBytes = new byte[keyBytes.length];
            for (int i = 0; i < keyBytes.length; i++) {
                xorBytes[i] = (byte) (keyBytes[i] ^ 0x5A);
            }
            String encoded = Base64.getEncoder().encodeToString(xorBytes);
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            Files.writeString(file.toPath(), encoded);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store credential", e);
        }
    }

    /**
     * Loads the key by reading the file, reversing Base64 + XOR obfuscation.
     * Returns null if the file is missing or corrupted.
     */
    public String load() {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try {
            String encoded = Files.readString(file.toPath()).trim();
            byte[] xorBytes = Base64.getDecoder().decode(encoded);
            byte[] keyBytes = new byte[xorBytes.length];
            for (int i = 0; i < xorBytes.length; i++) {
                keyBytes[i] = (byte) (xorBytes[i] ^ 0x5A);
            }
            return new String(keyBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            // corrupted or tampered file — return null
            return null;
        }
    }

    /**
     * Clears the stored credential by deleting the credentials file.
     * Returns true if the file was deleted, false if it didn't exist.
     */
    public boolean clear() {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }
}