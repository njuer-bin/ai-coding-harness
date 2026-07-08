package com.codingagent.model;

import java.util.List;

public class MemoryEntry {
    private String id;
    private String content;
    private String type; // CONVENTION | DECISION | CONTEXT | FEEDBACK
    private long timestamp;
    private List<String> tags;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}