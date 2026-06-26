package com.jadecaravan.domain.campaign;

public record CaravanStats(
        int offense,
        int defense,
        int mobility,
        int morale) {

    public CaravanStats {
        DomainValidation.requireRangeInclusive(offense, "offense", 0, 10);
        DomainValidation.requireRangeInclusive(defense, "defense", 0, 10);
        DomainValidation.requireRangeInclusive(mobility, "mobility", 0, 10);
        DomainValidation.requireRangeInclusive(morale, "morale", 0, 10);
    }
}
