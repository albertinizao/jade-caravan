package com.jadecaravan.adapter.in.web.campaign;

import com.jadecaravan.adapter.in.web.campaign.dto.CampaignDayCreateRequest;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignDayCloseRequest;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignDayPlanRequest;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignDayReopenRequest;
import com.jadecaravan.adapter.in.web.campaign.dto.CampaignDayResolveRequest;
import com.jadecaravan.application.campaign.port.in.CampaignDailyCycleUseCase;
import com.jadecaravan.domain.campaign.CampaignDay;
import com.jadecaravan.domain.campaign.CampaignDayPreview;
import com.jadecaravan.domain.campaign.CampaignDaySummary;
import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
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
public class CampaignDailyCycleController {

    private final CampaignDailyCycleUseCase campaignDailyCycleUseCase;

    public CampaignDailyCycleController(CampaignDailyCycleUseCase campaignDailyCycleUseCase) {
        this.campaignDailyCycleUseCase = campaignDailyCycleUseCase;
    }

    @GetMapping("/{campaignId}/days")
    public CampaignDailyCycleState getState(@PathVariable UUID campaignId) {
        return campaignDailyCycleUseCase.getState(campaignId);
    }

    @PostMapping("/{campaignId}/days")
    public CampaignDailyCycleState createDay(@PathVariable UUID campaignId, @RequestBody CampaignDayCreateRequest request) {
        return campaignDailyCycleUseCase.createDay(
                campaignId,
                request.campaignDay(),
                request.actor(),
                request.source(),
                request.reason());
    }

    @PostMapping("/{campaignId}/days/{dayId}/plan")
    public CampaignDailyCycleState planDay(
            @PathVariable UUID campaignId,
            @PathVariable UUID dayId,
            @RequestBody CampaignDayPlanRequest request) {
        return campaignDailyCycleUseCase.planDay(
                campaignId,
                dayId,
                request.roleAssignments(),
                request.passengerAssignments(),
                request.towingAssignments(),
                request.dailyOperations(),
                request.overrideBlockers(),
                request.overrideReason(),
                request.actor(),
                request.source());
    }

    @GetMapping("/{campaignId}/days/{dayId}/preview")
    public CampaignDayPreview previewDay(@PathVariable UUID campaignId, @PathVariable UUID dayId) {
        return campaignDailyCycleUseCase.previewDay(campaignId);
    }

    @PostMapping("/{campaignId}/days/{dayId}/resolve")
    public CampaignDailyCycleState resolveDay(
            @PathVariable UUID campaignId,
            @PathVariable UUID dayId,
            @RequestBody CampaignDayResolveRequest request) {
        return campaignDailyCycleUseCase.resolveDay(
                campaignId,
                dayId,
                request.checkResolutions(),
                request.caravanEvents(),
                request.tradeTransactions(),
                request.actor(),
                request.source(),
                request.reason());
    }

    @PostMapping("/{campaignId}/days/{dayId}/close")
    public CampaignDaySummary closeDay(
            @PathVariable UUID campaignId,
            @PathVariable UUID dayId,
            @RequestBody(required = false) CampaignDayCloseRequest request) {
        CampaignDayCloseRequest effectiveRequest = request == null
                ? new CampaignDayCloseRequest("Director de juego", "daily-cycle", "Cierre del día")
                : request;
        return campaignDailyCycleUseCase.closeDay(
                campaignId,
                dayId,
                effectiveRequest.actor(),
                effectiveRequest.source(),
                effectiveRequest.reason());
    }

    @PostMapping("/{campaignId}/days/{dayId}/reopen")
    public CampaignDailyCycleState reopenDay(
            @PathVariable UUID campaignId,
            @PathVariable UUID dayId,
            @RequestBody CampaignDayReopenRequest request) {
        return campaignDailyCycleUseCase.reopenDay(
                campaignId,
                dayId,
                request.actor(),
                request.source(),
                request.reason());
    }
}
