package com.jadecaravan.domain.rules;

public enum RuleDecisionStatus {
    PENDING,
    RESOLVED;

    public boolean isBlockingAutomation() {
        return this == PENDING;
    }
}
