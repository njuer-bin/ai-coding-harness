package com.codingagent.model;

import java.util.Map;

public class Action {
    private String type;
    private Map<String, Object> parameters;

    public Action() {}

    public Action(String type, Map<String, Object> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}