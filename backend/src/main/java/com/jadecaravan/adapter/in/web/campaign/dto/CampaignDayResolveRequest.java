package com.jadecaravan.adapter.in.web.campaign.dto;

import com.jadecaravan.domain.campaign.CaravanEvent;
import com.jadecaravan.domain.campaign.CheckResolution;
import com.jadecaravan.domain.campaign.TradeTransaction;
import java.util.List;

public record CampaignDayResolveRequest(
        List<CheckResolution> checkResolutions,
        List<CaravanEvent> caravanEvents,
        List<TradeTransaction> tradeTransactions,
        String actor,
        String source,
        String reason) {
}
