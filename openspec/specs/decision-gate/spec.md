# Decisions Pending Before Automation

## Purpose

This specification defines the campaign decisions that MUST remain configurable or manual until the director explicitly resolves them.

## Requirements

### 1. Rounding for extended space

The system MUST treat the rounding rule for expanded passenger and cargo capacity as configurable until the campaign decides it.

The current default proposal is ceiling rounding with the documented minimum floor.

### 2. Stacked horse teams

The system MUST treat the exact stacking behavior of four-, six-, and eight-horse upgrades as a configurable rule until confirmed.

### 3. Discontent gain from event tables

The system MUST keep event-table discontent gain configurable until the campaign decides whether severity follows the failed-loss column or a fixed value.

### 4. Consecutive discontent days

The system MUST expose the consecutive-discontent counter and MUST let the campaign decide whether it resets on any no-discontent day and how it behaves after day 10.

### 5. Stove scope

The stove effect MUST remain configurable until the campaign decides whether it applies to travellers, carts, or camp units.

### 6. Fridge and ice timing

The exact perishable-calendar interaction between fridge and ice MUST remain configurable until the campaign chooses a final formula.

### 7. Heat insulation

The system MUST keep heat insulation penalty configurable until confirmed by campaign decision.

### 8. Tavern cart

The `Carro taberna` MUST remain a custom campaign catalog item until a real rule exists.

### 9. Trade settlement math

The gross-versus-net interpretation of trade income MUST remain configurable until the campaign chooses one.

### 10. Role ordering

The order in which role bonuses, teamwork, servant bonuses, role caps, hero bonuses, feats, carts, equipment, and penalties are applied MUST remain explicit and campaign-configurable.

### 11. Consumption reducing feats

Stacking rules for `Consumo eficiente` and `Organización impecable` MUST remain explicit until the campaign chooses the final reduction model.

### 12. Celebration stacking

The system MUST keep celebration stacking and replacement behavior configurable until resolved.

### 13. Destruction rules (resolved)

The campaign-specific destruction rule takes precedence over the generic rule. The applied campaign decision is `2d10+5`, and the system MUST make that resolved choice explicit.

### 14. Slavery subsystem

The slavery-related rules MUST be modelled as an optional subsystem that is disabled by default for new campaigns.

## Acceptance intent

- Given an unresolved decision, the system exposes it as a configuration or manual choice.
- Given a resolved decision, the implementation can lock the behavior and remove the ambiguity.
