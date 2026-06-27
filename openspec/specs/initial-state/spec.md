# Initial State

## Purpose

This specification captures the observed Excel snapshot as import seed data and highlights the data-quality issues that must be handled before live play.

Excel snapshot: jade-caravan/docs/Caravana Regente de Jade.xlsx

## Requirements

### 1. Observed snapshot

The system SHOULD be able to import or represent the observed starting data for:

- 28 carts;
- 80 travellers;
- 63 beasts;
- 126 total consumption;
- 347 salary total.

### 2. Cart snapshot

The import layer SHOULD preserve the current hit points, maximum hit points, towing state, passenger occupancy, and cargo occupancy observed in the spreadsheet.

### 3. Upgrade snapshot

The import layer SHOULD preserve observed upgrades such as armored carts and extended passenger space.

### 4. Beast snapshot

The import layer SHOULD preserve the observed beast counts and their assignments.

### 5. Cargo snapshot

The import layer SHOULD preserve the non-zero cargo rows observed in the spreadsheet, including supplies, charcoal, ice, cooking equipment, repair materials, magical materials, local goods, treasure, and camp equipment.

### 6. Role snapshot

The import layer SHOULD preserve the observed active roles where the spreadsheet provides enough information.

### 7. Import warnings

The system SHOULD surface the following issues as import warnings or exceptions to verify:

- a family carriage with 6.5 of 6 passengers may require an explicit campaign exception;
- `Carro taberna` exists in the spreadsheet but not in the shared rules catalog; it's a custom instance of `Carro vacío`.
- the sheet does not expose all caravan base stats or feat assignments;
- some driver assignments must be recreated or imported;
- some vehicles have accumulated damage, so current HP MUST be imported, not only damage.

## Acceptance intent

- Given the spreadsheet snapshot, the importer preserves the visible state instead of re-deriving it.
- Given incomplete source data, the system raises warnings rather than silently inventing values.
