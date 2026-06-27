package com.jadecaravan.adapter.in.web.campaign.dto;

import com.jadecaravan.domain.campaign.CampaignDay;

public record CampaignDayCreateRequest(
        CampaignDay campaignDay,
        String actor,
        String source,
        String reason) {
}
