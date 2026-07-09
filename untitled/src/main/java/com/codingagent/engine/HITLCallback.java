package com.codingagent.engine;

import com.codingagent.model.Action;

/**
 * Callback interface for Human-In-The-Loop approval.
 * Implementations should present a Y/N prompt to the user and return
 * true to approve execution, false to reject.
 */
@FunctionalInterface
public interface HITLCallback {
    boolean confirm(Action action);
}