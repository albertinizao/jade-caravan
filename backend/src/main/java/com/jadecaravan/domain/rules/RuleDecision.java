package com.jadecaravan.domain.rules;

import java.time.Instant;
import java.util.Objects;

public record RuleDecision(
        RuleDecisionKey key,
        RuleDecisionStatus status,
        String currentResolution,
        String configurationValue,
        String reason,
        String actor,
        String source,
        Instant resolvedAt) {

    public RuleDecision {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    public static RuleDecision pending(RuleDecisionKey key) {
        return new RuleDecision(key, RuleDecisionStatus.PENDING, null, null, null, null, null, null);
    }

    public static RuleDecision resolved(
            RuleDecisionKey key,
            String currentResolution,
            String configurationValue,
            String reason) {
        return resolved(key, currentResolution, configurationValue, reason, null, null, Instant.now());
    }

    public static RuleDecision resolved(
            RuleDecisionKey key,
            String currentResolution,
            String configurationValue,
            String reason,
            Instant resolvedAt) {
        return resolved(key, currentResolution, configurationValue, reason, null, null, resolvedAt);
    }

    public static RuleDecision resolved(
            RuleDecisionKey key,
            String currentResolution,
            String configurationValue,
            String reason,
            String actor,
            String source,
            Instant resolvedAt) {
        Objects.requireNonNull(currentResolution, "currentResolution must not be null");
        return new RuleDecision(key, RuleDecisionStatus.RESOLVED, currentResolution, configurationValue, reason, actor, source, resolvedAt);
    }

    public RuleDecision resolve(String currentResolution, String configurationValue, String reason) {
        return resolved(key, currentResolution, configurationValue, reason);
    }

    public RuleDecision resolve(String currentResolution, String configurationValue, String reason, String actor, String source) {
        return resolved(key, currentResolution, configurationValue, reason, actor, source, Instant.now());
    }

    public boolean blockingAutomation() {
        return status.isBlockingAutomation();
    }
}
