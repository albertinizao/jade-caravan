# 00. Fuentes, alcance y normas de automatización

## 1. Naturaleza de la aplicación

La aplicación es una **herramienta de dirección de juego**. Debe automatizar aritmética, restricciones y registros, pero no decidir resultados narrativos ni interpretar reglas ambiguas por cuenta propia.

Cada regla que requiera una decisión del director se implementará mediante:

- una propuesta calculada;
- una comprobación preparada con modificadores desglosados;
- una resolución manual o mediante dado opcional;
- una anotación de la decisión tomada;
- una entrada inmutable en el historial.

## 2. Fuentes de verdad

### 2.1 Reglas de campaña

| Prioridad | Fuente | Uso |
|---:|---|---|
| 1 | `Carros Updated.pdf` | Sobrescribe reglas concretas: trineos, aislamiento para el frío, espacio extendido separado, nevera, patines de hielo, estufa, combustible y oficios añadidos. |
| 2 | `Reglas de Caravana.pdf` | Regla operativa general de esta campaña. |
| 3 | `Guia jugador Regente de jade.pdf` | Completa fórmulas y procedimientos ausentes, sin recuperar reglas que hayan sido modificadas. |
| 4 | Excel de la caravana | Estado de partida, inventario y distribución vigente. |

### 2.2 Regla de prevalencia por conflicto

Cuando dos fuentes definan el mismo concepto:

1. aplicar la versión de `Carros Updated.pdf`;
2. si no existe, aplicar `Reglas de Caravana.pdf`;
3. si sigue sin existir, aplicar la guía oficial;
4. si aún falta información, no inventar una regla silenciosamente: registrar una decisión de campaña en `RuleDecision`.

## 3. Qué debe automatizarse

- sumas de puntos de golpe, capacidad, consumo y salarios;
- validación de alojamiento, carga, conductor y tiro;
- cálculo de velocidad y modificadores visibles;
- aplicación de límites de carro, rol, dote y mejora;
- control de usos por día, semana, mes y trimestre;
- generación de CD y desglose de modificadores;
- producción, desgaste, caducidad, combustible y reparación;
- historial de eventos y cambios de estado;
- alertas de ilegalidad o incoherencia.

## 4. Qué debe quedar en manos del director

- el tipo exacto de terreno, su modificador de viaje y los peligros narrativos;
- si una criatura cumple un requisito de rol cuando la ficha no está modelada;
- si un viaje, acción o evento cuenta como “significativo”;
- el resultado de tiradas físicas, salvo que se use el dado opcional de la app;
- qué hechizo concede una deidad con Bendición del camino;
- quién puede usar realmente un carro arcano, una estufa o una mejora contextual;
- la interpretación de reglas marcadas como ambiguas;
- el contenido de encuentros y combates tácticos.

## 5. Versionado de reglas

La aplicación debe almacenar una versión de reglas activa por campaña:

```text
RuleSetVersion
- id
- campaignId
- name
- effectiveFromDay
- notes
- isActive
```

Los eventos y cálculos cerrados deben apuntar a la versión aplicada. Cambiar una regla hoy no debe alterar retrospectivamente un día ya resuelto.

## 6. Convenciones de datos

- Día de campaña: entero secuencial, no fecha real obligatoria.
- Moneda: piezas de cobre (`cp`) como unidad interna.
- Capacidad y ocupación: `BigDecimal`, pues existen valores de `0.5`.
- Temperatura: guardar Fahrenheit como valor canónico y mostrar Celsius como conversión.
- Tiradas: guardar dado natural, modificadores, CD, resultado total y resultado narrativo.
- Inventario: representar lotes, no solo un contador global, cuando el origen, la caducidad o el carro importen.

## 7. Requisito de auditoría

Una operación que cambie recursos, daño, roles, alojamientos, tiro, descontento o estado de un día debe crear un `LedgerEntry` o `EventLog`.

No se permite editar silenciosamente un total histórico. Las correcciones se hacen mediante una operación de ajuste con motivo.
