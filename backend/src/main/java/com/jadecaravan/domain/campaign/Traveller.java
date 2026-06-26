package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record Traveller(
        UUID id,
        UUID caravanId,
        String name,
        boolean playerCharacter,
        boolean humanoid,
        String size,
        long foodConsumption,
        BigDecimal occupancyUnits,
        boolean countsAsTraveller,
        boolean needsRest,
        boolean needsFood,
        int baseAttackBonus,
        int hitDice,
        boolean alive,
        boolean conscious,
        String status,
        TravellerContract contract,
        List<TravellerRelation> relations,
        List<RoleCapability> roleCapabilities,
        List<DailyRoleAssignment> dailyRoleAssignments) {

    public Traveller {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(caravanId, "caravanId");
        DomainValidation.requireNonBlank(name, "name");
        DomainValidation.requireNonBlank(size, "size");
        DomainValidation.requireNonNegative(foodConsumption, "foodConsumption");
        DomainValidation.requireNonNegative(occupancyUnits, "occupancyUnits");
        DomainValidation.requireRangeInclusive(baseAttackBonus, "baseAttackBonus", Integer.MIN_VALUE, Integer.MAX_VALUE);
        DomainValidation.requireRangeInclusive(hitDice, "hitDice", Integer.MIN_VALUE, Integer.MAX_VALUE);
        DomainValidation.requireNonBlank(status, "status");
        relations = DomainCollections.immutableCopy(relations);
        roleCapabilities = DomainCollections.immutableCopy(roleCapabilities);
        dailyRoleAssignments = DomainCollections.immutableCopy(dailyRoleAssignments);
        for (DailyRoleAssignment assignment : dailyRoleAssignments) {
            if (!id.equals(assignment.travellerId())) {
                throw new IllegalArgumentException("dailyRoleAssignments must belong to the traveller");
            }
        }
    }

    public Traveller withContract(TravellerContract newContract) {
        return new Traveller(
                id,
                caravanId,
                name,
                playerCharacter,
                humanoid,
                size,
                foodConsumption,
                occupancyUnits,
                countsAsTraveller,
                needsRest,
                needsFood,
                baseAttackBonus,
                hitDice,
                alive,
                conscious,
                status,
                newContract,
                relations,
                roleCapabilities,
                dailyRoleAssignments);
    }

    public Traveller withRelation(TravellerRelation relation) {
        DomainValidation.requireNonNull(relation, "relation");
        return new Traveller(
                id,
                caravanId,
                name,
                playerCharacter,
                humanoid,
                size,
                foodConsumption,
                occupancyUnits,
                countsAsTraveller,
                needsRest,
                needsFood,
                baseAttackBonus,
                hitDice,
                alive,
                conscious,
                status,
                contract,
                DomainCollections.append(relations, relation),
                roleCapabilities,
                dailyRoleAssignments);
    }

    public Traveller withRoleCapability(RoleCapability capability) {
        DomainValidation.requireNonNull(capability, "capability");
        return new Traveller(
                id,
                caravanId,
                name,
                playerCharacter,
                humanoid,
                size,
                foodConsumption,
                occupancyUnits,
                countsAsTraveller,
                needsRest,
                needsFood,
                baseAttackBonus,
                hitDice,
                alive,
                conscious,
                status,
                contract,
                relations,
                DomainCollections.append(roleCapabilities, capability),
                dailyRoleAssignments);
    }

    public Traveller withDailyRoleAssignment(DailyRoleAssignment assignment) {
        DomainValidation.requireNonNull(assignment, "assignment");
        if (!id.equals(assignment.travellerId())) {
            throw new IllegalArgumentException("assignment must belong to the traveller");
        }
        return new Traveller(
                id,
                caravanId,
                name,
                playerCharacter,
                humanoid,
                size,
                foodConsumption,
                occupancyUnits,
                countsAsTraveller,
                needsRest,
                needsFood,
                baseAttackBonus,
                hitDice,
                alive,
                conscious,
                status,
                contract,
                relations,
                roleCapabilities,
                DomainCollections.append(dailyRoleAssignments, assignment));
    }
}
