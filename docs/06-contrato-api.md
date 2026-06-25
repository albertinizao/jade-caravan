# 06. Contrato API propuesto

Base: `/api/v1`.

## 1. Convenciones

- JSON en camelCase.
- IDs UUID.
- Errores RFC 9457 / Problem Details.
- Las mutaciones usan un `reason` opcional y generan auditoría.
- Las operaciones que cambian recursos aceptan `expectedVersion` para evitar sobrescrituras concurrentes.

## 2. Lecturas principales

| Método | Ruta | Propósito |
|---|---|---|
| GET | `/campaigns/{id}/dashboard` | Tablero y alertas calculadas. |
| GET | `/campaigns/{id}/caravan` | Estado global de caravana. |
| GET | `/campaigns/{id}/carts` | Carros y sus valores efectivos. |
| GET | `/campaigns/{id}/travellers` | Viajeros, capacidades y estado. |
| GET | `/campaigns/{id}/inventory` | Lotes, ubicación y alertas. |
| GET | `/campaigns/{id}/beasts` | Bestias, tiro y fatiga. |
| GET | `/campaigns/{id}/days/{day}` | Día, plan, eventos y resumen. |
| GET | `/campaigns/{id}/calculation-preview` | Previsión del estado con parámetros. |
| GET | `/catalogs/{catalogName}` | Catálogos de reglas. |

## 3. Carros

```text
POST   /campaigns/{id}/carts
PATCH  /campaigns/{id}/carts/{cartId}
POST   /campaigns/{id}/carts/{cartId}/upgrades
DELETE /campaigns/{id}/carts/{cartId}/upgrades/{upgradeId}
POST   /campaigns/{id}/carts/{cartId}/damage
POST   /campaigns/{id}/carts/{cartId}/repair
POST   /campaigns/{id}/carts/{cartId}/passengers
POST   /campaigns/{id}/carts/{cartId}/cargo-allocations
```

`POST /damage` debe devolver una previsualización de destrucción si los PG llegarían a cero.

## 4. Viajeros, roles y relaciones

```text
POST   /campaigns/{id}/travellers
PATCH  /campaigns/{id}/travellers/{travellerId}
POST   /campaigns/{id}/travellers/{travellerId}/relations
POST   /campaigns/{id}/days/{day}/role-assignments
DELETE /campaigns/{id}/days/{day}/role-assignments/{assignmentId}
POST   /campaigns/{id}/days/{day}/lodging-assignments
POST   /campaigns/{id}/salaries/pay
```

Respuesta de rol inválido:

```json
{
  "type": "https://caravan.local/problems/role-limit",
  "title": "No se puede asignar el rol",
  "status": 422,
  "detail": "El límite de Adivino es 1 y ya hay un viajero activo.",
  "violations": [{"field": "roleType", "code": "ROLE_LIMIT_REACHED"}]
}
```

## 5. Tiro y bestias

```text
POST /campaigns/{id}/beasts
POST /campaigns/{id}/days/{day}/towing-assignments
DELETE /campaigns/{id}/days/{day}/towing-assignments/{id}
GET  /campaigns/{id}/days/{day}/towing-validation
```

La validación debe devolver, por carro, fuerza requerida, fuerza actual, máximos de criaturas, estado y previsión de fatiga.

## 6. Inventario y economía

```text
POST /campaigns/{id}/inventory/lots
POST /campaigns/{id}/inventory/transfer
POST /campaigns/{id}/inventory/consume
POST /campaigns/{id}/days/{day}/trade/sell-merchandise
POST /campaigns/{id}/days/{day}/trade/sell-treasure
POST /campaigns/{id}/days/{day}/trade/buy
POST /campaigns/{id}/days/{day}/gifts
```

Una venta no se confirma hasta que exista una `CheckResolution` válida si requiere tirada.

## 7. Día de campaña y tiradas

```text
POST  /campaigns/{id}/days
PATCH /campaigns/{id}/days/{day}
POST  /campaigns/{id}/days/{day}/plan
POST  /campaigns/{id}/days/{day}/checks
POST  /campaigns/{id}/days/{day}/events
POST  /campaigns/{id}/days/{day}/close-preview
POST  /campaigns/{id}/days/{day}/close
POST  /campaigns/{id}/days/{day}/reopen
```

Ejemplo de resolución manual de tirada:

```json
{
  "checkType": "SECURITY",
  "dc": 22,
  "naturalRoll": 14,
  "manualOutcome": "SUCCESS",
  "notes": "La tormenta obliga a rodear la grieta."
}
```

## 8. Dotes y versión de reglas

```text
POST /campaigns/{id}/feats
PATCH /campaigns/{id}/feats/{featInstanceId}
POST /campaigns/{id}/feats/reassign-after-rest
GET  /campaigns/{id}/rules/active
POST /campaigns/{id}/rules/versions
POST /campaigns/{id}/rules/decisions
```

## 9. Endpoint de tablero

Debe devolver una estructura preparada para UI:

```json
{
  "summary": {
    "cartCount": 28,
    "travellers": {"current": 78, "capacity": 117},
    "cargo": {"current": 0, "capacity": 0},
    "dailyConsumption": 126,
    "speedMiles": 0,
    "discontent": 0
  },
  "alerts": [
    {"severity": "ERROR", "code": "MISSING_DRIVER", "cartId": "..."},
    {"severity": "WARNING", "code": "BEAST_FATIGUE_SOON", "cartId": "..."}
  ],
  "modifierBreakdowns": []
}
```
