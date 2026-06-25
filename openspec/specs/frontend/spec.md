# Frontend Specification

## Purpose

This specification defines the Vue 3 user experience and presentation rules for the caravan application.

## Requirements

### 1. UX goal

The UI MUST let the director determine, quickly and without guesswork:

- whether the caravan can depart;
- which resource will fail first;
- which carts or travellers violate a rule;
- which rolls must be requested at the table;
- what changed since the previous day.

### 2. Routes

The frontend MUST provide screens for:

- campaign selection;
- dashboard;
- day planner and close flow;
- carts;
- travellers;
- inventory;
- beasts;
- commerce;
- history/audit;
- rules and decisions.

### 3. Dashboard

The dashboard MUST show:

- operative carts over total;
- caravan hit points;
- travellers housed versus capacity;
- cargo occupied versus capacity;
- current consumption and provisions remaining;
- projected speed;
- discontent, morale, and mutiny state;
- beasts free, assigned, and near fatigue;
- unpaid salaries.

Alerts MUST be ordered by severity: error, warning, info.

### 4. Cart management

Cart rows MUST expose the cart name, type, current/max hit points, hardness, towing, passengers, cargo, driver, upgrades, and alerts.

Cart detail views MUST include passengers, cargo, towing, upgrades, damage/repair, and caravan benefit breakdowns.

### 5. Day planner

The planner MUST be step-based, not a single long form.

It MUST separate context, people, beasts, resources, preview, resolution, and close confirmation.

The close action MUST stay disabled while hard errors remain unresolved, unless the director has explicitly accepted an exception.

### 6. Essential interactions

The UI SHOULD support drag-and-drop for lodging, towing, and cargo allocation.

The UI MUST provide visible rule requirements, validation previews, and explicit check preparation.

Undo is allowed only while the day remains in draft; after that, corrections MUST use audited adjustments.

### 7. Accessibility and clarity

The UI MUST not rely on color alone.

Every important modifier MUST be explainable through a tooltip or detail panel.

The frontend MUST not duplicate critical business formulas; it MUST rely on backend results for final calculations.

### 8. Vue state

The application SHOULD use separate stores for campaign, caravan, day planning, catalogs, and notifications.

Local drafts MAY exist, but decisive validation MUST come from the backend.

## Acceptance intent

- Given a hard blocker, the close button is disabled.
- Given a modifier, the UI can show its origin.
- Given a day change, the user can see what changed since yesterday.
