package com.jadecaravan.domain.campaign;

import java.util.List;
import java.util.UUID;

public record Campaign(
        UUID id,
        String name,
        String ruleSetVersionId,
        Caravan caravan,
        List<CampaignAuditEntry> auditEntries) {

    public Campaign {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonBlank(name, "name");
        DomainValidation.requireNonBlank(ruleSetVersionId, "ruleSetVersionId");
        auditEntries = DomainCollections.immutableCopy(auditEntries);
        if (caravan != null && !id.equals(caravan.campaignId())) {
            throw new IllegalArgumentException("caravan must belong to the campaign");
        }
    }

    public Campaign withCaravan(Caravan newCaravan) {
        DomainValidation.requireNonNull(newCaravan, "newCaravan");
        if (!id.equals(newCaravan.campaignId())) {
            throw new IllegalArgumentException("newCaravan must belong to the campaign");
        }
        return new Campaign(id, name, ruleSetVersionId, newCaravan, auditEntries);
    }

    public Campaign withAuditEntry(CampaignAuditEntry auditEntry) {
        DomainValidation.requireNonNull(auditEntry, "auditEntry");
        if (!id.equals(auditEntry.campaignId())) {
            throw new IllegalArgumentException("auditEntry must belong to the campaign");
        }
        return new Campaign(id, name, ruleSetVersionId, caravan, DomainCollections.append(auditEntries, auditEntry));
    }
}
