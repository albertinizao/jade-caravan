package com.jadecaravan.domain.campaign;

import java.util.Map;
import java.util.UUID;

public record CaravanFeatInstance(
        UUID id,
        UUID caravanId,
        String featKey,
        Map<String, String> parameters,
        boolean consumed,
        String notes) {

    public CaravanFeatInstance {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(caravanId, "caravanId");
        DomainValidation.requireNonBlank(featKey, "featKey");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    public CaravanFeatInstance withConsumed(boolean newConsumed) {
        return new CaravanFeatInstance(id, caravanId, featKey, parameters, newConsumed, notes);
    }

    public CaravanFeatInstance withNotes(String newNotes) {
        return new CaravanFeatInstance(id, caravanId, featKey, parameters, consumed, newNotes);
    }
}
