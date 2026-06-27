package com.jadecaravan.application.campaign.port.in;

import com.jadecaravan.domain.campaign.CampaignDay;
import com.jadecaravan.domain.campaign.CampaignDayPreview;
import com.jadecaravan.domain.campaign.CampaignDaySummary;
import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
import com.jadecaravan.domain.campaign.CaravanEvent;
import com.jadecaravan.domain.campaign.CheckResolution;
import com.jadecaravan.domain.campaign.CartPassengerAssignment;
import com.jadecaravan.domain.campaign.DailyOperation;
import com.jadecaravan.domain.campaign.DailyRoleAssignment;
import com.jadecaravan.domain.campaign.TowingAssignment;
import com.jadecaravan.domain.campaign.TradeTransaction;
import java.util.List;
import java.util.UUID;

public interface CampaignDailyCycleUseCase {

    CampaignDailyCycleState getState(UUID campaignId);

    CampaignDailyCycleState createDay(UUID campaignId, CampaignDay campaignDay, String actor, String source, String reason);

    CampaignDailyCycleState planDay(
            UUID campaignId,
            UUID dayId,
            List<DailyRoleAssignment> roleAssignments,
            List<CartPassengerAssignment> passengerAssignments,
            List<TowingAssignment> towingAssignments,
            List<DailyOperation> dailyOperations,
            boolean overrideBlockers,
            String overrideReason,
            String actor,
            String source);

    CampaignDayPreview previewDay(UUID campaignId);

    CampaignDailyCycleState resolveDay(
            UUID campaignId,
            UUID dayId,
            List<CheckResolution> checkResolutions,
            List<CaravanEvent> caravanEvents,
            List<TradeTransaction> tradeTransactions,
            String actor,
            String source,
            String reason);

    CampaignDaySummary closeDay(UUID campaignId, UUID dayId, String actor, String source, String reason);

    CampaignDailyCycleState reopenDay(UUID campaignId, UUID dayId, String actor, String source, String reason);
}
