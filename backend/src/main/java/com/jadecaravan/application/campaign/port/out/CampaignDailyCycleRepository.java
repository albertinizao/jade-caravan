package com.jadecaravan.application.campaign.port.out;

import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
import java.util.Optional;
import java.util.UUID;

public interface CampaignDailyCycleRepository {

    Optional<CampaignDailyCycleState> findByCampaignId(UUID campaignId);

    CampaignDailyCycleState save(CampaignDailyCycleState state);
}
