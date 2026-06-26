package com.jadecaravan.application.campaign.port.out;

import com.jadecaravan.domain.campaign.CampaignAuditEntry;
import java.util.List;
import java.util.UUID;

public interface CampaignAuditRepository {

    void append(CampaignAuditEntry entry);

    List<CampaignAuditEntry> loadTrail(UUID campaignId);
}
