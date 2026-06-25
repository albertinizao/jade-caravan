# Acceptance Tests

## Purpose

This specification records the minimum behavioral contract that the implementation MUST satisfy.

## Requirements

### A. Capacity and transport

#### AT-01 — Excess passengers blocks travel

Given a cart with capacity 4 and passengers at 4.5, when travel is planned, then the plan MUST be invalid with `PASSENGER_CAPACITY_EXCEEDED`.

#### AT-02 — Fractional occupancy on a family carriage

Given a family carriage with two occupants at 0.5 each, when they are lodged, then they MUST consume one total slot and the breakdown MUST keep both travellers visible.

#### AT-03 — Special cargo cart does not mix cargo types

Given a special cargo cart with iron, when charcoal is assigned, then the operation MUST be rejected unless both cargo items share the same explicit subtype.

#### AT-04 — Museum cart only accepts treasure

Given a museum cart, when supplies are assigned, then the API MUST return `CARGO_TYPE_NOT_ALLOWED`.

### B. Roles

#### AT-05 — One driver per operative cart

Given two operative carts and one driver, when the plan is validated, then the second cart MUST be reported as `MISSING_DRIVER`.

#### AT-06 — Hero does not consume the ordinary slot

Given a PC with Hero and Guard, when the day is calculated, then both effects MUST apply and Hero MUST NOT consume the ordinary slot.

#### AT-07 — Oracle limit

Given one active Oracle, when a second Oracle is assigned, then the assignment MUST be rejected.

#### AT-08 — Farmer outside a garden cart

Given a traveller with Farmer lodged in a normal cart, when Agriculture is assigned, then the assignment MUST be marked invalid.

### C. Towing and beasts

#### AT-09 — Insufficient towing strength

Given a cart requiring 10 and beasts totaling 9, when travel is planned, then that cart MUST not move.

#### AT-10 — Fatigue after five days

Given towing between 100% and less than 150% of the required strength, when five consecutive travel days are closed, then the beasts MUST become fatigued.

#### AT-11 — Double strength avoids towing fatigue

Given towing strength at or above 200% of required strength, when twenty travel days are closed, then no fatigue MUST be added for that reason.

### D. Consumption and inventory

#### AT-12 — Scout does not consume

Given a traveller with consumption 1 and active Scout role, when consumption is calculated, then that traveller MUST not be counted.

#### AT-13 — Cook converts supplies

Given one supply unit and an active cook, when the unit is consumed, then the system MUST produce 15 provisions and reduce the lot by one unit.

#### AT-14 — Perishable goods degrade

Given an unprotected perishable lot, when two days are closed, then it MUST lose one provision of value.

#### AT-15 — Fridge removes passenger slots

Given a cart with a fridge, when capacity is calculated, then passenger capacity MUST be zero even if later space expansion exists.

### E. Speed and upgrades

#### AT-16 — Updated cold insulation penalty

Given one or more carts with cold insulation, when speed is calculated, then the system MUST apply a single `-4 miles/day` penalty, not `-1` per cart.

#### AT-17 — Ice runners outside ice

Given a cart with ice runners on non-frozen terrain, when propulsion is calculated, then required strength MUST be multiplied by four.

#### AT-18 — Global improved wheels

Given all operative carts have improved wheels, when speed is calculated, then the system MUST add `+8 miles/day`.

### F. Discontent and mutiny

#### AT-19 — Mutiny at equality

Given discontent equal to effective morale, when the day is closed, then mutiny MUST exist even if the excess penalty is zero.

#### AT-20 — Mutiny by excess

Given morale 5 and discontent 8, when a check is calculated, then the result MUST include `-3` from mutiny.

#### AT-21 — Gift of treasure

Given the last treasure gift was one unit, when two units are gifted, then discontent MUST drop by three and the tracked reference MUST update to two.

### G. Damage and destruction

#### AT-22 — Repair consumes material

Given a driver, a stationary day, and one repair material, when a valid repair is resolved, then exactly one unit MUST be consumed and `15 × level` hit points MUST be restored.

#### AT-23 — Protected destruction

Given Levelling from Nothing not yet used, when a cart would receive lethal damage, then it MUST remain at 0 HP, gain the trait, and not be destroyed.

#### AT-24 — Second destruction is normal

Given Levelling from Nothing already consumed, when another cart reaches 0 HP, then normal destruction consequences MUST occur.

### H. Auditability

#### AT-25 — Closed day is immutable

Given a closed day, when consumption is edited, then the API MUST reject the edit and require an audited adjustment.

#### AT-26 — Reproducible breakdown

Given a closed daily summary, when it is recalculated from its rule version and history, then the totals MUST match exactly.

## Acceptance intent

- The implementation MUST satisfy each scenario as a deterministic, testable behavior.
- The acceptance suite MUST cover breakdowns, blockers, and auditability, not only final totals.
