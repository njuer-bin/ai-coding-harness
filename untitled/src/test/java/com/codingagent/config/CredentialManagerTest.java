package com.codingagent.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CredentialManagerTest {

    @Test
    void testStoreAndLoad(@TempDir Path tempDir) {
        CredentialManager cm = new CredentialManager(tempDir.resolve("cred.json").toString());
        cm.store("my-secret-key");
        assertEquals("my-secret-key", cm.load());
    }

    @Test
    void testLoadMissingFile(@TempDir Path tempDir) {
        CredentialManager cm = new CredentialManager(tempDir.resolve("nonexistent.json").toString());
        assertNull(cm.load());
    }

    @Test
    void testTamperedKey(@TempDir Path tempDir) {
        CredentialManager cm = new CredentialManager(tempDir.resolve("cred.json").toString());
        cm.store("real-key");
        // overwrite with a different key
        cm.store("tampered-key");
        assertEquals("tampered-key", cm.load());
    }
}