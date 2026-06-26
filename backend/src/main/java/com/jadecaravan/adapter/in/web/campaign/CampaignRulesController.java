package com.jadecaravan.adapter.in.web.campaign;

import com.jadecaravan.adapter.in.web.campaign.dto.CampaignRuleDecisionRequest;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignRuleDecisionResponse;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignRuleGateSummaryResponse;
import com.jadecaravan.application.campaign.port.in.CampaignRulesUseCase;
import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleGateSummary;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/api/v1/campaigns", produces = MediaType.APPLICATION_JSON_VALUE)
public class CampaignRulesController {

    private final CampaignRulesUseCase campaignRulesUseCase;

    public CampaignRulesController(CampaignRulesUseCase campaignRulesUseCase) {
        this.campaignRulesUseCase = campaignRulesUseCase;
    }

    @GetMapping("/{campaignId}/rules/decisions")
    public List<CampaignRuleDecisionResponse> getDecisions(@PathVariable UUID campaignId) {
        return campaignRulesUseCase.getDecisions(campaignId).stream()
                .map(CampaignRulesController::toResponse)
                .toList();
    }

    @PostMapping("/{campaignId}/rules/decisions")
    public CampaignRuleDecisionResponse resolveDecision(
            @PathVariable UUID campaignId,
            @Valid @RequestBody CampaignRuleDecisionRequest request) {
        RuleDecision resolvedDecision = campaignRulesUseCase.resolveDecision(
                campaignId,
                request.decisionKey(),
                request.reason(),
                request.configurationValue());
        return toResponse(resolvedDecision);
    }

    @GetMapping("/{campaignId}/rules/active")
    public CampaignRuleGateSummaryResponse getActiveSummary(@PathVariable UUID campaignId) {
        RuleGateSummary summary = campaignRulesUseCase.getActiveSummary(campaignId);
        return new CampaignRuleGateSummaryResponse(
                summary.automationBlocked(),
                summary.unresolvedDecisions().stream()
                        .map(CampaignRulesController::toResponse)
                        .toList());
    }

    private static CampaignRuleDecisionResponse toResponse(RuleDecision decision) {
        RuleDecisionKey key = decision.key();
        return new CampaignRuleDecisionResponse(
                key,
                key.title(),
                key.description(),
                key.defaultProposal(),
                decision.status(),
                decision.currentResolution(),
                decision.configurationValue(),
                decision.reason(),
                decision.blockingAutomation());
    }
}
