package com.codingagent.memory;

import com.codingagent.model.MemoryEntry;

import java.util.List;

public interface Memory {
    void store(MemoryEntry entry);
    List<MemoryEntry> retrieve(String query);
}