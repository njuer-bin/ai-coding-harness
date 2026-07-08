package com.codingagent.memory;

import com.codingagent.model.MemoryEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryImpl implements Memory {
    private final File file;
    private final ObjectMapper mapper;
    private final List<MemoryEntry> entries;

    public MemoryImpl(String filePath) {
        this.file = new File(filePath);
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.entries = new ArrayList<>(loadFromFile());
    }

    private List<MemoryEntry> loadFromFile() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(file, new TypeReference<List<MemoryEntry>>() {});
        } catch (IOException e) {
            // corrupted file — silently rebuild with empty storage
            return new ArrayList<>();
        }
    }

    private void saveToFile() {
        try {
            mapper.writeValue(file, entries);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist memory entries", e);
        }
    }

    @Override
    public synchronized void store(MemoryEntry entry) {
        entries.add(entry);
        saveToFile();
    }

    @Override
    public synchronized List<MemoryEntry> retrieve(String query) {
        if (query == null || query.isEmpty() || "*".equals(query)) {
            return new ArrayList<>(entries);
        }
        String lowerQuery = query.toLowerCase();
        return entries.stream()
                .filter(e -> (e.getContent() != null && e.getContent().toLowerCase().contains(lowerQuery))
                        || (e.getTags() != null && e.getTags().stream()
                                .anyMatch(t -> t != null && t.toLowerCase().contains(lowerQuery))))
                .collect(Collectors.toList());
    }
}