package com.jadecaravan.application.campaign.port.out;

import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleResolutionAuditEntry;
import java.util.List;
import java.util.UUID;

public interface CampaignRulesRepository {

    CampaignRuleState loadOrCreate(UUID campaignId);

    void save(CampaignRuleState state);

    void appendAuditEntry(RuleResolutionAuditEntry entry);

    List<RuleResolutionAuditEntry> loadAuditTrail(UUID campaignId);
}
