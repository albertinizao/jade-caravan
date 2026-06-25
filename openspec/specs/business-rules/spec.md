# Business Rules

## Purpose

This specification defines the caravan mechanics that govern validation, resource usage, combat-readiness, movement, and campaign economy.

## Requirements

### 1. Caravan stats

The caravan MUST start with base stats at `1` and three additional distributable points.

Level progression MUST grant feats, but it MUST NOT automatically raise base stats unless a feat does so.

### 2. Derived checks

Attack, Armor Class, Security, and Resolve/Determination MUST resolve as d20-based checks that include the relevant base stat and modifiers.

Armor Class MUST be treated as a check when needed, not as a static defense score.

### 3. Capacity and movement

Passenger occupancy and cargo occupancy MUST be compared against the sum of effective cart capacities.

If passenger or cargo occupancy exceeds capacity, the caravan MUST not travel normally.

### 4. Driver requirement

Each operative cart MUST have a daily assigned driver.

If a cart lacks a driver, that cart MUST not move and the caravan MUST not perform a normal travel day unless the cart is explicitly left behind.

### 5. Cart restrictions

Cart-type restrictions MUST be enforced, including:

- slave carts only accept slaves;
- prisoner carts only accept prisoners;
- zoo carts reject humanoids;
- special cargo carts enforce subtype purity;
- supply carts only accept supplies and perishables;
- museum carts only accept treasure;
- garden carts only allow housed/assigned travellers to act as farmers;
- school carts allow at most two active teachers;
- oracle carts enable the Oracle role only when operative;
- medical carts double long-term care for assigned passengers.

### 6. Towing and fatigue

Four-legged or larger creatures MUST count double for towing strength.

If towing strength is below the cart requirement, the cart MUST not move.

The system MUST track consecutive towing days per cart/beast assignment and apply fatigue thresholds at the documented percentages.

### 7. Speed and travel

Speed MUST be computed from the slowest towing creature and then adjusted by terrain, feats, upgrades, night travel, fatigue, mutiny, and other context modifiers.

The system MUST expose the full speed breakdown.

### 8. Consumption and supplies

Daily consumption MUST include counting travellers who eat, excluding active scouts/batidores, plus operative cart consumption.

Supply units MUST convert to provisions according to the catalog, and cooks MAY improve the yield according to the documented rule.

If provisions are insufficient, the caravan MUST suffer the documented damage/fatigue consequences and MUST not silently recover.

### 9. Perishables, fridge, ice, cold, and heat

Perishable cargo MUST degrade over time and MUST be tracked per lot.

Fridge carts MUST remove passenger capacity permanently and MUST interact with perishables and ice as documented.

Cold and heat insulation, stove, charcoal, and firewood MUST apply the documented temperature and speed effects.

Open decisions about stove scope, fridge/ice calendar details, and heat insulation MUST remain configurable until resolved by campaign decision.

### 10. Damage, repair, and destruction

Repair MUST consume repair materials, require the proper role, and restore hit points according to the repair rule.

Destruction MUST be auditable and MUST expose a strong confirmation step and consequence preview.

The campaign-specific destruction damage to crew MUST take precedence over the generic rule set when there is a conflict.

### 11. Discontent and mutiny

Discontent MUST never fall below zero.

Mutiny MUST occur when discontent is at least equal to effective morale.

The mutiny penalty MUST be applied to checks as documented.

The event-based discontent increase table MUST remain configurable until the ambiguity is resolved.

### 12. Fleeing

The flee action MUST be represented as a resolved operation with its own check and resulting movement consequence.

### 13. Trade, salaries, and economy

Trade transactions MUST be recorded with source, destination, modifiers, and resulting income.

The system MUST keep salary obligations visible and payable through audited operations.

### 14. Feats

Feats MUST be activatable and deactivatable according to their requirements, durations, and daily/weekly/monthly usage limits.

Campaign-specific feat decisions such as consumption reduction, celebration stacking, and slavery rules MUST stay explicit and configurable where the docs mark them unresolved.

## Acceptance intent

- Given a cart without a driver, the travel validation blocks it.
- Given insufficient towing strength, the cart does not move.
- Given insufficient provisions, the caravan suffers the documented consequences instead of a silent failure.
