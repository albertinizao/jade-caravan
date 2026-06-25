# Domain Model

## Purpose

This specification defines the core aggregates, entities, and derived-state boundaries for the caravan domain.

## Requirements

### 1. Aggregate structure

The domain model MUST center on `Campaign` and `Caravan`.

`Caravan` MUST own or reference:

- caravan stats;
- feat instances;
- travellers;
- carts;
- beasts;
- inventory lots;
- campaign days;
- check resolutions;
- caravan events;
- trade transactions;
- ledger entries.

### 2. Caravan

`Caravan` MUST store base stats separately from derived modifiers.

Base offensive, defensive, mobility, and morale values MUST remain within `0..10`.

### 3. Cart

`Cart` MUST store the identity and health of the vehicle.

Effective cart properties MUST be derived from the cart type and its upgrades, not stored as the only source of truth.

### 4. Traveller

`Traveller` MUST represent any creature that participates in the caravan except creatures actively serving as towing beasts.

`occupancyUnits` MUST allow zero and fractional occupancy.

The model MUST allow travellers that do not count as travellers for consumption, lodging, or role accounting.

### 5. Role assignment

`DailyRoleAssignment` MUST capture the role for a specific day and target context such as cart, traveller, skill, or language.

A traveller MUST NOT hold more than one ordinary role per day unless the rules explicitly allow an exception such as Hero, Spellcaster, or Cross-Training.

### 6. Beast and towing

A beast MUST cease counting as a traveller while assigned as towing or pulling force.

`TowingAssignment` MUST be day-scoped and cart-scoped.

### 7. Inventory lot

`InventoryLot` MUST preserve quantity, unit value, location, and special metadata required for special cargo types.

The model MUST support perishable goods, treasure, local goods, altar cargo, magical materials, ammunition, and subtype-restricted cargo.

### 8. Campaign day

`CampaignDay` MUST represent one planning and resolution unit with a status lifecycle.

A closed day MUST not be edited directly.

### 9. Check resolution

`CheckResolution` MUST store the check type, modifiers, DC, roll, outcome, and notes for reproducibility.

### 10. Caravan event

`CaravanEvent` MUST store source, severity, narrative summary, and whether the event requires a check.

### 11. Ledger entry

`LedgerEntry` MUST be immutable and MUST capture material deltas with a reason and related event when applicable.

### 12. Derived state

The following values MUST be derived, not treated as primary truth:

- caravan total HP;
- global passenger and cargo capacity;
- daily consumption;
- speed;
- role bonuses;
- overage car count;
- towing sufficiency;
- beast fatigue;
- mutiny alerts;
- pending salary.

## Acceptance intent

- Given a traveller with `occupancyUnits = 0.5`, the model preserves the fractional occupancy.
- Given a beast assigned to towing, the beast no longer counts as a traveller for that day.
- Given a closed day, direct mutation is not the normal path.
