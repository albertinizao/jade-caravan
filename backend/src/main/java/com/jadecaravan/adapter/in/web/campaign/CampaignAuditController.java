package com.jadecaravan.adapter.in.web.campaign;

import com.jadecaravan.adapter.in.web.campaign.dto.CampaignAuditEntryResponse;
import com.jadecaravan.application.campaign.port.in.CampaignAuditUseCase;
import com.jadecaravan.domain.campaign.CampaignAuditEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/api/v1/campaigns", produces = MediaType.APPLICATION_JSON_VALUE)
public class CampaignAuditController {

    private final CampaignAuditUseCase campaignAuditUseCase;

    public CampaignAuditController(CampaignAuditUseCase campaignAuditUseCase) {
        this.campaignAuditUseCase = campaignAuditUseCase;
    }

    @GetMapping("/{campaignId}/audit")
    public List<CampaignAuditEntryResponse> getAuditTrail(@PathVariable UUID campaignId) {
        return campaignAuditUseCase.getAuditTrail(campaignId).stream()
                .map(CampaignAuditController::toResponse)
                .toList();
    }

    private static CampaignAuditEntryResponse toResponse(CampaignAuditEntry entry) {
        return new CampaignAuditEntryResponse(
                entry.ruleSetVersionId(),
                entry.entryType(),
                entry.subjectType(),
                entry.subjectId(),
                entry.operationType(),
                entry.title(),
                entry.subjectId(),
                entry.currentResolution(),
                entry.configurationValue(),
                entry.reason(),
                entry.actor(),
                entry.source(),
                entry.occurredAt());
    }
}
