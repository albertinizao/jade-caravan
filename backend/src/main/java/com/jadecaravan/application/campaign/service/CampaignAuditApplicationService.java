package com.jadecaravan.application.campaign.service;

import com.jadecaravan.application.campaign.port.in.CampaignAuditUseCase;
import com.jadecaravan.application.campaign.port.out.CampaignAuditRepository;
import com.jadecaravan.domain.campaign.CampaignAuditEntry;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CampaignAuditApplicationService implements CampaignAuditUseCase {

    private final CampaignAuditRepository campaignAuditRepository;

    public CampaignAuditApplicationService(CampaignAuditRepository campaignAuditRepository) {
        this.campaignAuditRepository = Objects.requireNonNull(campaignAuditRepository, "campaignAuditRepository must not be null");
    }

    @Override
    public List<CampaignAuditEntry> getAuditTrail(UUID campaignId) {
        return campaignAuditRepository.loadTrail(campaignId);
    }
}
