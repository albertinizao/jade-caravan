# Calculation Engine

## Purpose

This specification defines the pure rules engine that calculates caravan totals, breakdowns, warnings, and blockers.

## Requirements

### 1. Purity

The calculation engine MUST be a Java-only domain library.

It MUST NOT depend on HTTP, JPA, or UI code.

### 2. Output contract

Every calculation MUST return a result object containing:

- value or total;
- breakdown items;
- warnings;
- blockers;
- rule set version identifier.

Uncertain mechanics MUST return a proposed resolution and MUST require a `CheckResolution`.

### 3. Calculation order

The engine MUST evaluate caravan state in this order:

1. select operative carts;
2. apply cart upgrades;
3. validate passengers, cargo, and cart restrictions;
4. validate towing and towing fatigue;
5. calculate global capacity and base consumption;
6. build active role effects;
7. build active feat effects;
8. build context effects such as terrain, climate, settlement, and camp;
9. apply excess-cart penalties;
10. apply mutiny, fatigue, and other temporary effects;
11. expose totals and a full breakdown.

### 4. Breakdown fidelity

The engine MUST expose the origin of every modifier.

The UI and API MUST be able to reproduce the same breakdown from the returned data.

### 5. Effective cart calculation

Effective cart properties MUST be derived from base cart data plus upgrades.

The engine MUST apply the documented upgrade effects and MUST represent unresolved rounding decisions as configurable campaign rules.

### 6. Role modifiers

Role bonuses MUST respect normal limits, expert bonuses, hero bonuses, teamwork, and servant assistance in the documented order.

The engine MUST keep the limited role pool separate from bonuses that are explicitly outside the limit.

### 7. Checks

Checks MUST be built from the base stat, source modifiers, penalty modifiers, DC, roll, and outcome.

The engine MUST expose natural roll, total modifier, DC, and success state.

### 8. Mutiny

Mutiny MUST be calculated from current discontent and effective morale.

The penalty MUST be visible as a separate modifier.

### 9. Speed

Speed MUST be calculated from towing creature speed, terrain multipliers, flat bonuses, and flat penalties.

The engine MUST define and expose rounding as an explicit policy.

### 10. Consumption

Consumption MUST account for eaten travellers, batidores, operative carts, feat effects, fasting, and celebration effects.

The engine MUST never let a reduction violate a documented floor.

### 11. Event-based discontent

Event-based discontent MUST be calculated with configurable severity handling until the campaign decision is settled.

### 12. Domain services

The codebase SHOULD separate the engine into clear services such as:

- caravan calculation;
- cart validation;
- towing validation;
- role assignment;
- inventory allocation;
- daily planning;
- daily resolution;
- discontent;
- trade;
- perishables;
- maintenance;
- feat activation.

### 13. Invariants

The engine MUST preserve these invariants:

- a day cannot close twice;
- a lot cannot become negative;
- a traveller cannot drive two carts on the same day unless explicitly allowed;
- a beast cannot tow two carts on the same day;
- a traveller cannot occupy more than their real occupancy;
- incompatible upgrades cannot coexist;
- temporal effects need origin and expiration.

## Acceptance intent

- Given the same input and rule version, the engine returns the same output.
- Given a required breakdown, the engine includes the source of each modifier.
- Given an invalid state, the engine returns blockers rather than silently correcting it.
