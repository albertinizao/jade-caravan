package com.jadecaravan.adapter.in.web.campaign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignRuleDecisionRequest;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignRuleDecisionResponse;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignRuleGateSummaryResponse;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleDecisionStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CampaignRulesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exposesSeededDecisionGateAndRuleSummaryEndpoints() throws Exception {
        UUID campaignId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        String decisionsJson = mockMvc.perform(get("/api/v1/campaigns/{campaignId}/rules/decisions", campaignId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<CampaignRuleDecisionResponse> decisions = objectMapper.readValue(
                decisionsJson,
                new TypeReference<>() {
                });

        assertThat(decisions).hasSize(14);
        assertThat(decisions.stream()
                .filter(decision -> decision.decisionKey() == RuleDecisionKey.D_13_DESTRUCTION_RULE_PRIORITY)
                .findFirst()
                .orElseThrow()
                .status())
                .isEqualTo(com.jadecaravan.domain.rules.RuleDecisionStatus.RESOLVED);

        CampaignRuleDecisionRequest request = new CampaignRuleDecisionRequest(
                RuleDecisionKey.D_02_TOWING_BONUS_STACKING,
                "Align the stacking rule with campaign guidance.",
                "+4 per installed towing tier");

        String resolvedJson = mockMvc.perform(post("/api/v1/campaigns/{campaignId}/rules/decisions", campaignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        CampaignRuleDecisionResponse resolvedDecision = objectMapper.readValue(
                resolvedJson,
                CampaignRuleDecisionResponse.class);

        assertThat(resolvedDecision.decisionKey()).isEqualTo(RuleDecisionKey.D_02_TOWING_BONUS_STACKING);
        assertThat(resolvedDecision.status()).isEqualTo(RuleDecisionStatus.RESOLVED);
        assertThat(resolvedDecision.currentResolution()).isEqualTo("+4 per installed towing tier");
        assertThat(resolvedDecision.configurationValue()).isEqualTo("+4 per installed towing tier");

        String activeJson = mockMvc.perform(get("/api/v1/campaigns/{campaignId}/rules/active", campaignId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        CampaignRuleGateSummaryResponse summary = objectMapper.readValue(
                activeJson,
                CampaignRuleGateSummaryResponse.class);

        assertThat(summary.automationBlocked()).isTrue();
        assertThat(summary.unresolvedDecisions()).hasSize(12);
    }
}
