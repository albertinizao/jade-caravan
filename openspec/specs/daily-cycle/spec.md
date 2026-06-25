# Daily Cycle

## Purpose

This specification defines the state machine and operational flow for one campaign day.

## Requirements

### 1. Day states

The day lifecycle MUST use the following states:

- `DRAFT`;
- `PLANNED`;
- `RESOLVING`;
- `CLOSED`.

A cancelled or reopened day MAY exist only through explicit audited operations.

### 2. Opening a day

The system MUST be able to create a campaign day with activity type, terrain, weather, temperature, settlement, planned travel hours, planned distance, and encounter context.

### 3. Planning

The system MUST allow the director to assign daily roles, lodging, drivers, towing, and special daily actions such as fasting, blessing, celebration, ice, charcoal, and repair.

### 4. Validation before departure

The system MUST block transition to `PLANNED` when there are unresolved hard errors such as:

- excess passengers or cargo;
- missing driver;
- insufficient propulsion;
- storage restriction violations;
- hard role limit violations;
- missing role requirements;
- invalid feat usage;
- insufficient inventory.

If the director overrides a blocker, the override MUST require a reason and MUST remain visible.

### 5. Preview

Before resolution, the system MUST show a preview of speed, consumption, morale, mutiny, beast fatigue, and relevant resource alerts without applying the change.

### 6. Resolution

The resolution phase MUST record checks, weather events, hazards, trade, combat, repairs, animal care, resource production, flight, damage, deaths, losses, and discontent.

The system MUST NOT decide the narrative content of events.

### 7. Closing order

Closing a day MUST execute in a deterministic order:

1. apply event results and damage;
2. apply flee or failed-progress effects;
3. calculate and consume supplies;
4. apply role and environment production;
5. degrade perishables and melt ice where applicable;
6. update beast fatigue and rest recovery;
7. apply discontent, mutiny, and reductions;
8. update daily, weekly, monthly, and quarterly usage counters;
9. close trade, salary, and chronicle records;
10. generate ledger entries and the daily summary.

### 8. Core daily operations

The system MUST represent travel, rest, civilised pause, repair, eating, celebration, gifting, fasting, and commerce as distinct operations with separate ledger effects.

### 9. Immutable close

A closed day MUST be immutable through normal editing.

Corrections MUST be done through audited adjustments or audited reopen flows.

### 10. Daily summary

Every closed day MUST produce a reproducible summary containing:

- planned versus actual distance;
- planned versus consumed supplies and any deficit;
- production;
- events and checks;
- damage and repairs;
- discontent before and after;
- inventory changes;
- alerts that continue to the next day.

## Acceptance intent

- Given a blocker, the day cannot advance to planned without an explicit exception.
- Given a closed day, direct edits are not accepted.
- Given a resolved day, the system emits a deterministic summary.
