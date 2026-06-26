package com.jadecaravan.domain.campaign;

import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
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
                upgradeInstances,
                passengerAssignments,
                cargoAllocations,
                towingAssignments);
    }
}
