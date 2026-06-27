package com.jadecaravan.application.campaign.initialstate;

import static org.assertj.core.api.Assertions.assertThat;

import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ObservedInitialStateSeedTest {

    @Test
    void preservesTheObservedExcelSnapshotWithoutReDerivingIt() {
        ObservedInitialStateSeed seed = ObservedInitialStateSeed.create(CatalogRegistry.seeded());
        UUID campaignId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        CampaignDailyCycleState state = seed.buildState(campaignId, seed.ruleSetVersionId());

        assertThat(seed.campaignName()).isEqualTo("Caravana Regente de Jade");
        assertThat(seed.summary().cartCount()).isEqualTo(28);
        assertThat(seed.summary().travellerCount()).isEqualTo(80);
        assertThat(seed.summary().beastCount()).isEqualTo(63);
        assertThat(seed.summary().consumptionTotal()).isEqualTo(126);
        assertThat(seed.summary().salaryTotalCp()).isEqualTo(34700);
        assertThat(seed.warnings()).hasSizeGreaterThanOrEqualTo(5);

        assertThat(state.caravan().carts()).hasSize(28);
        assertThat(state.caravan().travellers()).hasSize(80);
        assertThat(state.caravan().beasts()).hasSize(63);
        assertThat(state.caravan().campaignDays()).hasSize(1);
        assertThat(state.activeDay()).isNotNull();
        assertThat(state.caravan().name()).isEqualTo("Caravana Regente de Jade");
        assertThat(state.caravan().baseStats().offense()).isEqualTo(4);
        assertThat(state.caravan().baseStats().defense()).isEqualTo(4);
        assertThat(state.caravan().baseStats().mobility()).isEqualTo(4);
        assertThat(state.caravan().baseStats().morale()).isEqualTo(4);

        assertThat(state.caravan().carts())
                .filteredOn(cart -> cart.name().equals("Familias Kalsgard"))
                .anySatisfy(cart -> {
                    assertThat(cart.currentHitPoints()).isEqualTo(60);
                    assertThat(cart.assignedPassengerOccupancy()).isEqualByComparingTo("6.5");
                    assertThat(cart.cartType().passengerCapacity()).isEqualByComparingTo("6");
                });

        assertThat(state.caravan().carts())
                .filteredOn(cart -> cart.name().equals("Bar sin límites"))
                .anySatisfy(cart -> {
                    assertThat(cart.cartType().name()).isEqualTo("Carro taberna");
                    assertThat(cart.currentHitPoints()).isEqualTo(54);
                });
    }
}
