# 01. Modelo de dominio

## 1. Agregados principales

```text
Campaign
└── Caravan
    ├── CaravanStats
    ├── CaravanFeatInstance[*]
    ├── Traveller[*]
    │   ├── TravellerContract?
    │   ├── TravellerRelation[*]
    │   ├── RoleCapability[*]
    │   └── DailyRoleAssignment[*]
    ├── Cart[*]
    │   ├── CartUpgradeInstance[*]
    │   ├── CartPassengerAssignment[*]
    │   ├── CartCargoAllocation[*]
    │   └── TowingAssignment[*]
    ├── Beast[*]
    ├── InventoryLot[*]
    ├── CampaignDay[*]
    │   ├── CheckResolution[*]
    │   ├── Operation[*]
    │   ├── CaravanEvent[*]
    │   └── TradeTransaction[*]
    └── LedgerEntry[*]
```

## 2. Entidades y responsabilidades

### 2.1 `Caravan`

Estado global de la comunidad viajera.

Campos esenciales:

```text
id, campaignId, name, level
ruleSetVersionId
baseOffense, baseDefense, baseMobility, baseMorale
currentDiscontent
currentDayNumber
```

Los atributos base son enteros de `0..10`. Las bonificaciones externas se calculan aparte; no se deben sumar destructivamente al atributo base.

### 2.2 `Cart`

Un carro, carruaje o trineo individual.

```text
id, caravanId, name, cartTypeId
currentHitPoints, destroyed
notes
```

Sus estadísticas efectivas proceden del tipo y de sus mejoras:

```text
maxHp, hardness, propulsionStrength, maxTowingCreatures,
consumption, passengerCapacity, cargoCapacity, specialBenefits
```

### 2.3 `Traveller`

Cualquier criatura que forme parte de la caravana, excepto las bestias que estén actuando como tiro. Los familiares y algunas criaturas diminutas pueden configurarse como no contables.

```text
id, caravanId, name, category
isPlayerCharacter, isHumanoid, size, foodConsumption
occupancyUnits, countsAsTraveller, needsRest, needsFood
baseAttackBonus, hitDice, alive, conscious, status
```

`occupancyUnits` permite modelar menores o mascotas que cuentan como `0.5` o `0` plazas.

### 2.4 `RoleCapability` y `DailyRoleAssignment`

- `RoleCapability`: indica que un viajero puede ejercer un rol y con qué fundamento.
- `DailyRoleAssignment`: indica el rol elegido para un día de campaña concreto.

```text
DailyRoleAssignment
- travellerId
- campaignDayId
- roleType
- targetCartId?          // conductor, artillero, agricultor, profesor...
- targetTravellerId?     // sirviente -> señor
- targetSkill?           // profesor
- targetLanguage?        // instructor
- optionJson             // decisiones específicas de la jornada
```

No se permite más de un rol ordinario por viajero y día, salvo excepciones modeladas explícitamente: Héroe, Lanzador de conjuros y Entrenamiento cruzado.

### 2.5 `Beast` y `TowingAssignment`

```text
Beast
- id, caravanId, beastTypeId, name?
- currentHp, strength, speedFt, size
- trainedForCombat, temperatureAdaptation, fatigued
- activeAsTowing

TowingAssignment
- cartId, beastId, campaignDayId
```

Una bestia no cuenta como viajero mientras esté usada como bestia de carga/tiro.

### 2.6 `InventoryLot`

El inventario debe conservar ubicación y propiedades relevantes.

```text
id, caravanId, cargoTypeId
quantity, unitCapacity, unitValueCp
cartId?
originSettlementId?
remainingProvisions?
perishableDecayProgress?
metadataJson
```

Casos que requieren `metadataJson`:

- mercancías locales: asentamiento de origen y distancia acumulada;
- tesoro: valor, peso/capacidad, exhibible o no;
- suministros perecederos: provisiones restantes y calendario de degradación;
- munición: cantidad física por lote;
- altar: deidad consagrada;
- materiales mágicos: valor restante;
- cargamento específico: subtipo concreto.

### 2.7 `CampaignDay`

Unidad de planificación y cierre.

```text
id, caravanId, dayNumber, status
activityType: TRAVEL | REST | CIVILIZATION_PAUSE | WORK | CAMP | COMBAT
terrainType?, location?, settlementType?
temperatureF?, weatherSeverity?, travelHours
plannedDistanceMiles, resolvedDistanceMiles
```

Estados:

```text
DRAFT -> PLANNED -> RESOLVING -> CLOSED
```

Un día cerrado no se edita directamente. Se corrige con un ajuste o una reapertura auditada.

### 2.8 `CheckResolution`

Representa una comprobación de caravana.

```text
id, campaignDayId, checkType
baseStat, sourceModifiers[], penaltyModifiers[]
dc, naturalRoll?, total, success, degreeOfFailure
manualOutcome, notes
```

`checkType` incluye `ATTACK`, `ARMOR_CLASS`, `SECURITY`, `DETERMINATION`, `TRADE`, `LEADER_SPEECH`, `ESCAPE`, `REPAIR`, `ANIMAL_CARE` y otros.

### 2.9 `CaravanEvent`

Sucesos que causan consecuencias o descontento.

```text
id, campaignDayId, eventType, severity
source, narrativeSummary
requiresCheck, checkId?
resolved, effectsApplied
```

### 2.10 `LedgerEntry`

Registro inmutable de efectos materiales.

```text
id, campaignDayId, operationType
resourceType, resourceId?, delta
reason, relatedEventId?, createdAt
```

Ejemplos: `CONSUME_SUPPLIES`, `PRODUCE_PROVISIONS`, `APPLY_DAMAGE`, `REPAIR_CART`, `PAY_SALARY`, `SELL_CARGO`, `ADD_DISCONTENT`.

## 3. Relaciones relevantes

- Un viajero puede alojarse en un carro y conducir otro distinto.
- Un carro puede tener muchos pasajeros, lotes de carga y bestias de tiro.
- Un lote de carga solo puede estar en un carro operativo a la vez.
- Un viajero usado como bestia de tiro deja de computar como viajero durante ese día.
- Una dote puede tener múltiples instancias y parámetros: deidad, terreno, carro objetivo o número de selección.
- Una relación familiar o romántica se registra entre exactamente dos viajeros.

## 4. Estados derivados, no persistidos como fuente primaria

- puntos de golpe totales de caravana;
- capacidad y ocupación global de pasajeros y carga;
- consumo diario;
- velocidad;
- bonificadores de ataque, CA, seguridad y determinación;
- exceso de carros;
- propulsión suficiente por carro;
- fatiga de bestias;
- alerta de motín;
- salario mensual pendiente.

Estos valores salen de `CaravanCalculationService`.
