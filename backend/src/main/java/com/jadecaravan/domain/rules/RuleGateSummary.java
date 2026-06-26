package com.jadecaravan.domain.rules;

import java.util.List;

public record RuleGateSummary(
        boolean automationBlocked,
        List<RuleDecision> unresolvedDecisions) {

    public RuleGateSummary {
        unresolvedDecisions = List.copyOf(unresolvedDecisions);
    }
}
