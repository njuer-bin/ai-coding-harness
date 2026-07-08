package com.codingagent.model;

import java.util.ArrayList;
import java.util.List;

public class Context {
    private String taskDescription;
    private List<Message> conversation = new ArrayList<>();
    private List<Feedback> previousFeedback = new ArrayList<>();
    private List<MemoryEntry> relevantMemories = new ArrayList<>();

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    public List<Message> getConversation() { return conversation; }
    public void setConversation(List<Message> conversation) { this.conversation = conversation; }
    public List<Feedback> getPreviousFeedback() { return previousFeedback; }
    public void setPreviousFeedback(List<Feedback> previousFeedback) { this.previousFeedback = previousFeedback; }
    public List<MemoryEntry> getRelevantMemories() { return relevantMemories; }
    public void setRelevantMemories(List<MemoryEntry> relevantMemories) { this.relevantMemories = relevantMemories; }
}