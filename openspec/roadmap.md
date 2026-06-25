# Implementation Roadmap

This roadmap orders the specifications by dependency so the implementation can progress from foundations to user-facing workflows without rework.

## 1. Foundation and governance

1. `source-and-scope`
2. `decision-gate`
3. `catalogs`

Reason: before building behavior, the project needs source precedence, unresolved-decision handling, and versioned reference data.

## 2. Core domain

4. `domain-model`
5. `business-rules`

Reason: the domain model and rules define what the system must store and how it behaves.

## 3. Calculation core

6. `calculation-engine`

Reason: the pure engine depends on the domain and rules, and it becomes the source of truth for totals, warnings, and blockers.

## 4. Time and workflow

7. `daily-cycle`

Reason: the day lifecycle consumes the engine outputs and turns them into validated planning and closing flows.

## 5. Contracts and presentation

8. `api-contract`
9. `frontend`

Reason: the API must expose the engine and the day lifecycle before the UI can present and mutate the state safely.

## 6. Safety net and data bootstrapping

10. `acceptance-tests`
11. `initial-state`

Reason: acceptance tests lock behavior, and initial-state import protects the observed spreadsheet snapshot from being lost or reinterpreted.

## Recommended vertical slice order

If implementation needs a slimmer slice-by-slice sequence, use this order:

1. source-and-scope
2. domain-model
3. catalogs
4. calculation-engine
5. business-rules
6. daily-cycle
7. api-contract
8. frontend
9. acceptance-tests
10. initial-state
11. decision-gate follow-up as campaign decisions are resolved

## Notes

- The roadmap intentionally places the pure engine before the UI so the frontend only renders backend-calculated truth.
- The decision-gate spec stays active throughout the project because unresolved campaign choices must not be hidden in code.
