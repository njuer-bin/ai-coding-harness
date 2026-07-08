package com.codingagent.memory;

import com.codingagent.model.MemoryEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    @Test
    void testStoreAndRetrieve(@TempDir Path tempDir) {
        MemoryImpl memory = new MemoryImpl(tempDir.resolve("memory.json").toString());

        MemoryEntry entry1 = new MemoryEntry();
        entry1.setId("1");
        entry1.setContent("Project uses Java 21");
        entry1.setType("CONVENTION");
        entry1.setTimestamp(System.currentTimeMillis());
        entry1.setTags(List.of("java", "version"));

        MemoryEntry entry2 = new MemoryEntry();
        entry2.setId("2");
        entry2.setContent("Use TDD for all features");
        entry2.setType("CONVENTION");
        entry2.setTimestamp(System.currentTimeMillis());
        entry2.setTags(List.of("tdd", "process"));

        memory.store(entry1);
        memory.store(entry2);

        List<MemoryEntry> results = memory.retrieve("");
        assertEquals(2, results.size());
    }

    @Test
    void testRetrieveWithQuery(@TempDir Path tempDir) {
        MemoryImpl memory = new MemoryImpl(tempDir.resolve("memory.json").toString());

        MemoryEntry entry1 = new MemoryEntry();
        entry1.setId("1");
        entry1.setContent("Project uses Java 21");
        entry1.setType("CONVENTION");
        entry1.setTimestamp(System.currentTimeMillis());
        entry1.setTags(List.of("java", "version"));

        MemoryEntry entry2 = new MemoryEntry();
        entry2.setId("2");
        entry2.setContent("Use TDD for all features");
        entry2.setType("CONVENTION");
        entry2.setTimestamp(System.currentTimeMillis());
        entry2.setTags(List.of("tdd", "process"));

        memory.store(entry1);
        memory.store(entry2);

        List<MemoryEntry> results = memory.retrieve("Java");
        assertEquals(1, results.size());
        assertEquals("1", results.get(0).getId());
    }

    @Test
    void testEmptyStore(@TempDir Path tempDir) {
        MemoryImpl memory = new MemoryImpl(tempDir.resolve("memory.json").toString());
        List<MemoryEntry> results = memory.retrieve("anything");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testCorruptedFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("memory.json");
        Files.writeString(file, "not-valid-json{{{");
        // corrupted file should auto-rebuild with empty storage, no exception
        MemoryImpl memory = new MemoryImpl(file.toString());
        List<MemoryEntry> results = memory.retrieve("anything");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}