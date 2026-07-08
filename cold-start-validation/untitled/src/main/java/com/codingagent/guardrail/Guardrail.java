package com.codingagent.guardrail;

import com.codingagent.model.Action;
import com.codingagent.model.enums.GuardrailResult;

public interface Guardrail {
    GuardrailResult check(Action action);
}