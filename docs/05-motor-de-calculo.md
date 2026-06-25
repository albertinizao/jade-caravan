# 05. Motor de cálculo

## 1. Principios

- El motor debe ser una librería Java pura, sin dependencias de HTTP ni JPA.
- Toda función devuelve el resultado y el desglose de por qué se obtuvo.
- Las funciones no mutan la base de datos: producen un `CalculationResult` o un `OperationPlan` que la capa de aplicación confirma.
- Las reglas con incertidumbre generan una propuesta y requieren una `CheckResolution`.

## 2. Orden de cálculo del estado de caravana

```text
1. Seleccionar carros operativos.
2. Aplicar mejoras a cada carro.
3. Validar pasajeros, carga y restricciones de carro.
4. Validar tiro y obtener estado de fatiga por carro.
5. Calcular capacidad global y consumo base.
6. Construir efectos de roles activos.
7. Construir efectos de dotes activas.
8. Construir efectos de contexto: terreno, clima, asentamiento, campamento.
9. Aplicar penalización por exceso de carros.
10. Aplicar estado de motín, fatiga y otros efectos temporales.
11. Exponer totales y un desglose de cada modificador.
```

## 3. Fórmulas

### 3.1 Carro efectivo

```java
record EffectiveCart(
    int maxHp,
    int hardness,
    int propulsionStrength,
    int maxLargeTowers,
    int maxMediumTowers,
    int consumption,
    BigDecimal passengerCapacity,
    BigDecimal cargoCapacity,
    List<RuleEffect> effects
) {}
```

Aplicación orientativa:

```text
maxHp = baseHp
hardness = baseHardness
propulsion = basePropulsion
passengers = basePassengers
cargo = baseCargo

por cada mejora:
  ACORAZADO: maxHp = floor(maxHp * 1.5), hardness += 5, propulsion *= 2
  REFUERZO: maxHp += 10, cargo -= 1
  ESPACIO VIAJEROS: passengers += max(1, ceil(basePassengers * 0.25))
  ESPACIO CARGA: cargo += max(2, ceil(baseCargo * 0.25))
  NEVERA: passengers = 0 y marcar capacidad bloqueada
  PATINES: modificar propulsión según terreno
  TIROS: modificar fuerza, máximo de bestias y consumo
```

El criterio de redondeo de espacio ampliado está pendiente de confirmación; por defecto propuesto: redondear hacia arriba para no perder el mínimo indicado.

### 3.2 Bonificadores de roles

```text
bonificadorRolPorEstadistica = suma de efectos activos de roles
limiteNormal = +5
limiteConExpertos = +5 + instancias(Viajeros expertos)
```

Los Héroes no cuentan contra ese límite y pueden aportar hasta `+4` adicional a Seguridad y Determinación.

Trabajo en equipo:

```text
n = min(3, viajeros activos en mismo rol)
multiplicadorEquipo = 1 + 0.25 * (n - 1)
```

Sirviente sobre un señor con rol bonificador:

```text
multiplicadorSirviente = 1.0
si señor tiene rol con beneficio: +0.5
si sirviente cumple requisito del rol del señor: +0.5
```

Aplicar como aumento aditivo del rendimiento (`x1.5` o `x2.0`), no como multiplicación encadenada, salvo decisión de campaña posterior.

### 3.3 Tiradas

```text
modificadorTotal = atributoBase
                 + bonificadoresRolesLimitados
                 + bonificadoresFueraDeLimite
                 + dotes
                 + carros
                 + equipo
                 + contexto
                 + penalizaciones
                 + penalizacionMotin
```

```text
resultado = d20 + modificadorTotal
```

La interfaz debe separar:

- atributo base;
- cada fuente de bonificador;
- límite aplicado;
- penalizador por exceso de carros;
- penalizador por motín;
- dado natural;
- CD y resultado.

### 3.4 Motín

```text
moralEfectiva = moralBase + modificadoresDeMoral
amotinado = descontento >= moralEfectiva
exceso = max(0, descontento - moralEfectiva)
penalizacionMotin = -exceso
```

### 3.5 Velocidad

```text
velocidadPiesMinima = min(velocidad de bestias asignadas a carros que viajan)
millasBase = tablaVelocidad[velocidadPiesMinima]

millasFinales = millasBase
              * multiplicadorTerreno
              * multiplicadorEstado  // noche, fatiga, motín
              + bonificadoresPlanos
              - penalizacionesPlanas
```

El motor debe exponer primero cada componente, y solo al final aplicar una política de redondeo. Propuesta: redondear la distancia real hacia abajo a una décima de milla para no crear avance gratuito.

### 3.6 Consumo

```text
viajerosComen = sum(traveller.foodConsumption de viajeros contables)
                - sum(consumo de batidores activos)
consumoBase = viajerosComen + sum(consumo de carros operativos)
consumoDotes = aplicarConsumoEficienteYOrganizacion(consumoBase)
consumoAyuno = reducir solo componente de viajeros si procede
consumoCelebracion = duplicar consumo final si procede
```

Nunca permitir que una reducción baje por debajo del mínimo de consumo de carros cuando esa dote lo prohíba.

### 3.7 Descontento por evento

```text
para cada categoriaDeDescontento del día:
  severidad = maxima severidad registrada en categoria
  dcBase = {1:15, 2:20, 3:25}[severidad]
  dc = dcBase + 2 * elementosAdicionalesMismaSituacion
  preparar Determinacion con dc
  si falla: añadir el aumento de descontento configurado para la severidad
```

La magnitud exacta del aumento de descontento de esta tabla debe conservarse configurable hasta cerrar la ambigüedad descrita en `10-decidir-antes-de-automatizar.md`.

## 4. Servicios de dominio propuestos

```text
CaravanCalculationService
CartValidationService
TowingValidationService
RoleAssignmentService
InventoryAllocationService
DailyPlanningService
DailyResolutionService
DiscontentService
TradeService
PerishableService
MaintenanceService
FeatActivationService
```

## 5. Ejemplo de resultado calculado

```json
{
  "metric": "securityCheck",
  "base": 6,
  "modifiers": [
    {"source": "Guía", "value": 1},
    {"source": "Dos guardas", "value": 2},
    {"source": "Terreno conocido", "value": 2},
    {"source": "Exceso de carros", "value": -3}
  ],
  "totalModifier": 8,
  "requiresRoll": true
}
```

## 6. Invariantes de código

- No se puede cerrar un día dos veces.
- Un lote no puede tener cantidad negativa.
- Un viajero no puede conducir dos carros el mismo día salvo que una regla futura lo permita expresamente.
- Una bestia no puede estar asignada a dos carros el mismo día.
- Un viajero no puede ocupar plazas por encima de su ocupación real.
- Una mejora incompatible no puede coexistir en un carro.
- Un efecto temporal debe tener origen y fecha de expiración.
