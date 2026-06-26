package com.jadecaravan.application.campaign.port.out;

import com.jadecaravan.domain.rules.CampaignRuleState;
import java.util.UUID;

public interface CampaignRulesRepository {

    CampaignRuleState loadOrCreate(UUID campaignId);

    void save(CampaignRuleState state);
}
