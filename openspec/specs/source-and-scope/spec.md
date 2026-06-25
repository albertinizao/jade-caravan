# Source, Scope, and Automation Boundaries

## Purpose

This specification defines the authoritative sources, the automation boundary, and the audit rules for the caravan management application.

## Requirements

### 1. Source hierarchy

The system MUST resolve rule conflicts using the project precedence order:

1. campaign-specific later director decisions;
2. `Carros Updated.pdf`;
3. `Reglas de Caravana.pdf`;
4. `Guia jugador Regente de jade.pdf`;
5. imported Excel state only as observational seed data.

If a conflict cannot be resolved from those sources, the system MUST not invent a silent rule.
If a later director decision exists, it MUST override lower-precedence sources and MUST be stored as a new `RuleSetVersion` or an explicit decision record linked to one.

### 2. Automation boundary

The application MUST automate arithmetic, validation, resource accounting, and audit logging.

The application MUST NOT decide narrative consequences, hidden interpretations, or ambiguous rule outcomes without an explicit campaign decision or manual resolution.
The application MUST keep ambiguous outcomes visible to the user rather than collapsing them into a hidden default.

### 3. Ambiguities

Any unresolved ambiguity MUST be represented as one of the following:

- a configurable campaign option;
- a manual decision in the user flow;
- a blocking alert explaining what is missing.

### 4. Rule versioning

The system MUST store a `RuleSetVersion` per campaign and MUST attach that version to resolved days, calculations, events, and adjustments.

Changing a rule in the future MUST NOT retroactively alter a closed day.
Historical reads MUST be reproducible from the original `RuleSetVersion` and the immutable event/adjustment trail.

### 5. Data conventions

The system MUST use the project canonical representations:

- currency in copper pieces;
- fractional capacity as `BigDecimal`;
- integer hit points, attribute points, days, and uses;
- Fahrenheit as the canonical temperature value.

### 6. Auditability

Any state-changing operation MUST create an immutable audit entry.

Corrections to closed history MUST be recorded as adjustments, not destructive edits.
The audit trail MUST preserve the reason, actor, timestamp, and source of the change.

## Acceptance intent

- Given conflicting sources, the system surfaces the conflict and preserves precedence.
- Given an ambiguous rule, the system blocks automation until a campaign decision exists.
- Given a closed day, later changes do not rewrite the historical record.
- Given a historical recalculation, the result matches the original versioned inputs and audit trail.
