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

    public boolean hasDailyRole(String roleKey, UUID campaignDayId) {
        return hasDailyRole(roleKey, campaignDayId, null);
    }

    public boolean hasDailyRole(String roleKey, UUID campaignDayId, UUID targetCartId) {
        if (roleKey == null || campaignDayId == null) {
            return false;
        }
        String normalizedRoleKey = roleKey.trim();
        return dailyRoleAssignments.stream()
                .filter(assignment -> campaignDayId.equals(assignment.campaignDayId()))
                .filter(assignment -> targetCartId == null || targetCartId.equals(assignment.targetCartId()))
                .anyMatch(assignment -> assignment.role().key().equalsIgnoreCase(normalizedRoleKey));
    }

    public boolean isScoutOn(UUID campaignDayId) {
        return hasDailyRole("SCOUT", campaignDayId);
    }

    public boolean isTeacherOn(UUID campaignDayId, UUID targetCartId) {
        return hasDailyRole("TEACHER", campaignDayId, targetCartId);
    }

    public boolean isFarmerOn(UUID campaignDayId, UUID targetCartId) {
        return hasDailyRole("FARMER", campaignDayId, targetCartId);
    }

    public boolean isSlave() {
        return normalizedStatus().contains("slave")
                || normalizedContractType().contains("slave")
                || relations.stream().anyMatch(relation -> relation.relationType() == TravellerRelationType.SLAVE);
    }

    public boolean isPrisoner() {
        return normalizedStatus().contains("prison")
                || normalizedContractType().contains("prison");
    }

    public boolean isHumanoidCreature() {
        return humanoid;
    }

    private String normalizedStatus() {
        return status == null ? "" : status.trim().toLowerCase();
    }

    private String normalizedContractType() {
        return contract == null || contract.contractType() == null ? "" : contract.contractType().trim().toLowerCase();
    }
}
