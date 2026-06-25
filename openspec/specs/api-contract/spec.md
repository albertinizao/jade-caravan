# API Contract

## Purpose

This specification defines the REST API surface used by the application.

## Requirements

### 1. Conventions

The API MUST use `/api/v1` as the base path.

JSON payloads MUST use camelCase.

Identifiers MUST use UUIDs.

Errors MUST use RFC 9457 Problem Details.

Mutating operations SHOULD accept an optional reason and MUST create audit records.

Optimistic concurrency-sensitive operations MUST accept `expectedVersion`.

### 2. Read endpoints

The API MUST provide read endpoints for:

- dashboard;
- caravan state;
- carts;
- travellers;
- inventory;
- beasts;
- campaign day;
- calculation previews;
- catalogs.

### 3. Cart endpoints

The API MUST support creating, updating, upgrading, damaging, repairing, and assigning passengers/cargo to carts.

Damage preview endpoints MUST return destruction preview data when hit points would reach zero.

### 4. Traveller, role, and relation endpoints

The API MUST support traveller creation, updates, traveller relations, role assignment, lodging assignment, and salary payment.

Role assignment failures MUST return stable structured errors.

### 5. Towing and beasts

The API MUST support beast creation, towing assignment changes, and towing validation.

Towing validation MUST return, per cart, required strength, current strength, creature caps, state, and fatigue forecast.

### 6. Inventory and economy

The API MUST support inventory lots, transfers, consumption, sales, purchases, and gifts.

Sales that require a roll MUST not be confirmed until a valid `CheckResolution` exists.

### 7. Day and checks

The API MUST support day creation, updates, planning, checks, event registration, close preview, close, and reopen.

Manual check resolution MUST store the natural roll, DC, outcome, and notes.

### 8. Feats and rules

The API MUST support feat creation and updates, feat reassignment after rest, querying active rule versions, creating rule versions, and recording rule decisions.

### 9. Dashboard payload

The dashboard endpoint MUST return a UI-ready structure including:

- high-level summary;
- ordered alerts;
- modifier breakdowns.

## Acceptance intent

- Given invalid role assignment, the API returns a stable Problem Details payload.
- Given towing validation, the API returns cart-level towing sufficiency.
- Given the dashboard endpoint, the UI can render summary and alerts without recomputing the rules.
