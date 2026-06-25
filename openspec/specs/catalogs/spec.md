# Catalogs

## Purpose

This specification defines the structured, versioned reference data used by the rules engine.

## Requirements

### 1. Versioned reference data

Catalog entries MUST be stored as data, not scattered constants in business code.

Catalogs MUST be versionable and campaign-aware where needed.

### 2. Cart types

The cart catalog MUST cover:

- traveller carts;
- cargo carts;
- special carts;
- campaign-specific custom carts when the published rules do not define them.

Each cart type MUST define cost, hit points, hardness, propulsion requirement, towing creature limits, consumption, passenger capacity, cargo capacity, restrictions, and mechanical effects.

### 3. Upgrades

The upgrade catalog MUST define cost, restrictions, stacking rules, incompatibilities, and effect payloads.

The catalog MUST include the documented campaign update items such as cold insulation, frost runners, extended space, and the siege/weapon upgrades.

### 4. Cargo

The cargo catalog MUST include supplies, perishables, treasure, charcoal, ice, firewood, repair materials, trade goods, magical materials, camping items, and other campaign cargo.

Each cargo type MUST define capacity, value, degradation, and special metadata requirements.

### 5. Roles

The role catalog MUST define the role name, hard limit, requirement, and summarized benefit.

The catalog MUST include ordinary roles, specialists, slaves, servants, and the optional slavery-related subsystems while keeping optional features explicit.

### 6. Feats

The feat catalog MUST define requirement, effect, usage limits, stacking, and persistence rules.

The catalog MUST include feats that alter morale, consumption, trade, movement, or role effectiveness.

### 7. Towing beasts

The beast catalog MUST define price, strength, size, speed, and temperature adaptation for the common towing animals.

### 8. Campaign-specific items

Items observed in the Excel snapshot but not fully defined by the published rules MUST be represented as campaign-specific custom catalog entries until a campaign decision resolves them.

## Acceptance intent

- Given a cart type, the engine can load its rule payload without hardcoding.
- Given a custom campaign cart, the system can store it as data until a formal rule exists.
