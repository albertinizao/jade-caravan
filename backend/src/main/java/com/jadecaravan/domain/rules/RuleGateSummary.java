package com.jadecaravan.domain.rules;

import java.util.List;

public record RuleGateSummary(
        String ruleSetVersionId,
        boolean automationBlocked,
        List<RuleDecision> unresolvedDecisions) {

    public RuleGateSummary {
        unresolvedDecisions = List.copyOf(unresolvedDecisions);
    }
}
