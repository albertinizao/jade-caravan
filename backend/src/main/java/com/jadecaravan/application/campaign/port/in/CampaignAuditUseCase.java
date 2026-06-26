package com.jadecaravan.application.campaign.port.in;

import com.jadecaravan.domain.campaign.CampaignAuditEntry;
import java.util.List;
import java.util.UUID;

public interface CampaignAuditUseCase {

    List<CampaignAuditEntry> getAuditTrail(UUID campaignId);
}
