package com.jadecaravan.adapter.in.web.campaign.dto;

import com.jadecaravan.domain.campaign.CartPassengerAssignment;
import com.jadecaravan.domain.campaign.DailyOperation;
import com.jadecaravan.domain.campaign.DailyRoleAssignment;
import com.jadecaravan.domain.campaign.TowingAssignment;
import java.util.List;

public record CampaignDayPlanRequest(
        List<DailyRoleAssignment> roleAssignments,
        List<CartPassengerAssignment> passengerAssignments,
        List<TowingAssignment> towingAssignments,
        List<DailyOperation> dailyOperations,
        boolean overrideBlockers,
        String overrideReason,
        String actor,
        String source) {
}
