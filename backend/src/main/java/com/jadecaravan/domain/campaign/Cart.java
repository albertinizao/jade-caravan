package com.jadecaravan.domain.campaign;

import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record Cart(
        UUID id,
        UUID caravanId,
        String name,
        CartTypeCatalogEntry cartType,
        int currentHitPoints,
        boolean destroyed,
        String notes,
        List<String> traits,
        List<CartUpgradeInstance> upgradeInstances,
        List<CartPassengerAssignment> passengerAssignments,
        List<CartCargoAllocation> cargoAllocations,
        List<TowingAssignment> towingAssignments) {

    public Cart {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(caravanId, "caravanId");
        DomainValidation.requireNonBlank(name, "name");
        DomainValidation.requireNonNull(cartType, "cartType");
        DomainValidation.requireNonNegative(currentHitPoints, "currentHitPoints");
        traits = traits == null ? List.of() : List.copyOf(traits);
        upgradeInstances = DomainCollections.immutableCopy(upgradeInstances);
        passengerAssignments = DomainCollections.immutableCopy(passengerAssignments);
        cargoAllocations = DomainCollections.immutableCopy(cargoAllocations);
        towingAssignments = DomainCollections.immutableCopy(towingAssignments);
        for (CartUpgradeInstance upgradeInstance : upgradeInstances) {
            if (!id.equals(upgradeInstance.cartId())) {
                throw new IllegalArgumentException("upgradeInstances must belong to the cart");
            }
        }
        for (CartPassengerAssignment passengerAssignment : passengerAssignments) {
            if (!id.equals(passengerAssignment.cartId())) {
                throw new IllegalArgumentException("passengerAssignments must belong to the cart");
            }
        }
        for (CartCargoAllocation cargoAllocation : cargoAllocations) {
            if (!id.equals(cargoAllocation.cartId())) {
                throw new IllegalArgumentException("cargoAllocations must belong to the cart");
            }
        }
        for (TowingAssignment towingAssignment : towingAssignments) {
            if (!id.equals(towingAssignment.cartId())) {
                throw new IllegalArgumentException("towingAssignments must belong to the cart");
            }
        }
    }

    public Cart withUpgradeInstance(CartUpgradeInstance upgradeInstance) {
        DomainValidation.requireNonNull(upgradeInstance, "upgradeInstance");
        if (!id.equals(upgradeInstance.cartId())) {
            throw new IllegalArgumentException("upgradeInstance must belong to the cart");
        }
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                currentHitPoints,
                destroyed,
                notes,
                traits,
                DomainCollections.append(upgradeInstances, upgradeInstance),
                passengerAssignments,
                cargoAllocations,
                towingAssignments);
    }

    public Cart withPassengerAssignment(CartPassengerAssignment passengerAssignment) {
        DomainValidation.requireNonNull(passengerAssignment, "passengerAssignment");
        if (!id.equals(passengerAssignment.cartId())) {
            throw new IllegalArgumentException("passengerAssignment must belong to the cart");
        }
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                currentHitPoints,
                destroyed,
                notes,
                traits,
                upgradeInstances,
                DomainCollections.append(passengerAssignments, passengerAssignment),
                cargoAllocations,
                towingAssignments);
    }

    public Cart withCargoAllocation(CartCargoAllocation cargoAllocation) {
        DomainValidation.requireNonNull(cargoAllocation, "cargoAllocation");
        if (!id.equals(cargoAllocation.cartId())) {
            throw new IllegalArgumentException("cargoAllocation must belong to the cart");
        }
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                currentHitPoints,
                destroyed,
                notes,
                traits,
                upgradeInstances,
                passengerAssignments,
                DomainCollections.append(cargoAllocations, cargoAllocation),
                towingAssignments);
    }

    public Cart withTowingAssignment(TowingAssignment towingAssignment) {
        DomainValidation.requireNonNull(towingAssignment, "towingAssignment");
        if (!id.equals(towingAssignment.cartId())) {
            throw new IllegalArgumentException("towingAssignment must belong to the cart");
        }
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                currentHitPoints,
                destroyed,
                notes,
                traits,
                upgradeInstances,
                passengerAssignments,
                cargoAllocations,
                DomainCollections.append(towingAssignments, towingAssignment));
    }

    public Cart withNotes(String newNotes) {
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                currentHitPoints,
                destroyed,
                newNotes,
                traits,
                upgradeInstances,
                passengerAssignments,
                cargoAllocations,
                towingAssignments);
    }

    public Cart withTrait(String trait) {
        DomainValidation.requireNonBlank(trait, "trait");
        if (hasTrait(trait)) {
            return this;
        }
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                currentHitPoints,
                destroyed,
                notes,
                DomainCollections.append(traits, trait.trim()),
                upgradeInstances,
                passengerAssignments,
                cargoAllocations,
                towingAssignments);
    }

    public Cart withTraits(List<String> newTraits) {
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                currentHitPoints,
                destroyed,
                notes,
                newTraits,
                upgradeInstances,
                passengerAssignments,
                cargoAllocations,
                towingAssignments);
    }

    public Cart withDestroyed(boolean newDestroyed) {
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                currentHitPoints,
                newDestroyed,
                notes,
                traits,
                upgradeInstances,
                passengerAssignments,
                cargoAllocations,
                towingAssignments);
    }

    public Cart withCurrentHitPoints(int newCurrentHitPoints) {
        DomainValidation.requireNonNegative(newCurrentHitPoints, "newCurrentHitPoints");
        return new Cart(
                id,
                caravanId,
                name,
                cartType,
                newCurrentHitPoints,
                destroyed,
                notes,
                traits,
                upgradeInstances,
                passengerAssignments,
                cargoAllocations,
                towingAssignments);
    }

    public boolean isOperative() {
        return !destroyed && currentHitPoints > 0;
    }

    public boolean hasActiveUpgrade(String upgradeKey) {
        if (upgradeKey == null) {
            return false;
        }
        String normalizedUpgradeKey = upgradeKey.trim();
        return upgradeInstances.stream()
                .filter(CartUpgradeInstance::active)
                .anyMatch(upgradeInstance -> upgradeInstance.upgrade().key().equalsIgnoreCase(normalizedUpgradeKey));
    }

    public boolean hasTrait(String trait) {
        if (trait == null) {
            return false;
        }
        String normalizedTrait = trait.trim();
        return traits.stream().anyMatch(existingTrait -> existingTrait.equalsIgnoreCase(normalizedTrait));
    }

    public List<CartUpgradeInstance> activeUpgrades() {
        return upgradeInstances.stream()
                .filter(CartUpgradeInstance::active)
                .toList();
    }

    public BigDecimal assignedPassengerOccupancy() {
        return passengerAssignments.stream()
                .map(CartPassengerAssignment::occupancyUnits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal assignedCargoQuantity() {
        return cargoAllocations.stream()
                .map(CartCargoAllocation::quantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
