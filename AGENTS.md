# AGENTS.md — Instrucciones para agentes de implementación

## 1. Propósito del proyecto

Este repositorio implementa una aplicación web de **gestión de caravana para Pathfinder 1.ª edición**, adaptada a la campaña **El Regente de Jade**.

La aplicación es una herramienta para el director de juego. Debe:

- conservar el estado de la caravana;
- calcular restricciones, costes y modificadores reproducibles;
- preparar comprobaciones de reglas;
- registrar decisiones, tiradas y cambios de recursos;
- avisar de incoherencias sin sustituir el criterio del director.

La aplicación **no** decide consecuencias narrativas, no interpreta silenciosamente ambigüedades y no sustituye la dirección de juego.

---

## 2. Lectura obligatoria antes de modificar código

Lee estos archivos en este orden antes de implementar o modificar comportamiento:

1. `README.md`
2. `00-fuentes-y-alcance.md`
3. `01-modelo-de-dominio.md`
4. `02-reglas-de-negocio.md`
5. `03-ciclo-diario.md`
6. `05-motor-de-calculo.md`
7. `08-pruebas-de-aceptacion.md`
8. `10-decidir-antes-de-automatizar.md`

Consulta además:

- `04-catalogos.md` para datos de catálogo, efectos y límites de carros, mejoras, carga, bestias, roles y dotes.
- `06-contrato-api.md` antes de modificar endpoints, DTOs o errores HTTP.
- `07-especificacion-frontend.md` antes de crear o cambiar pantallas y flujos de usuario.
- `09-estado-inicial.md` solo como fotografía de datos iniciales, **no como fuente de reglas**.

### Jerarquía de autoridad

Ante conflictos entre fuentes, aplica siempre este orden:

1. Decisiones posteriores del director documentadas como una nueva versión de reglas.
2. `Carros Updated.pdf`.
3. `Reglas de Caravana.pdf`.
4. `Guia jugador Regente de jade.pdf`.
5. `Caravana Regente de Jade(1).xlsx`, únicamente como estado inicial.

La traducción funcional de esas fuentes se encuentra en los Markdown de este repositorio. Si detectas una contradicción entre los documentos Markdown y una fuente superior, **no elijas una interpretación por tu cuenta**: documenta el conflicto y solicita o registra una `RuleDecision` explícita.

---

## 3. Principios no negociables

### 3.1 El motor de reglas es la fuente de los totales

No guardes como verdad primaria valores derivados como:

- consumo diario;
- velocidad;
- capacidades efectivas;
- ataque, CA, seguridad o determinación;
- modificadores de roles;
- propulsión requerida o disponible;
- descontento derivado de una operación;
- alertas de validez.

Esos datos se calculan desde el estado base, los efectos activos, la versión de reglas y las operaciones registradas. Se permite cachearlos, pero el sistema debe poder reconstruirlos exactamente desde cero.

### 3.2 Nada de reglas ocultas en la interfaz o en controladores

- Las fórmulas viven en el dominio o en servicios de aplicación dedicados al cálculo.
- Los controladores HTTP solo validan transporte, autorizan, invocan casos de uso y traducen respuestas.
- Los repositorios JPA no contienen reglas de negocio.
- Vue no recalcula reglas de forma independiente: presenta el desglose devuelto por el backend y envía intenciones de usuario.

### 3.3 Auditabilidad antes que comodidad

Toda operación que altere cualquiera de estos elementos debe generar un registro inmutable:

- inventario, moneda o carga;
- daño, reparación, destrucción o estado de un carro;
- alojamiento, tiro o roles diarios;
- descontento, moral, dotes, usos limitados o efectos temporales;
- estado, tiradas, distancia, clima o resultado de un día;
- versiones y decisiones de reglas.

No sobrescribas silenciosamente hechos históricos. Una corrección es una **operación de ajuste con motivo**, nunca una edición destructiva.

### 3.4 Un día cerrado es inmutable

Los días cerrados no se editan directamente. Para corregirlos:

1. crea una operación de ajuste auditada;
2. conserva la versión de reglas original;
3. recalcula los resúmenes afectados;
4. deja trazabilidad clara de quién, cuándo y por qué aplicó la corrección.

### 3.5 Las ambigüedades son configuración o decisión del director

Los puntos de `10-decidir-antes-de-automatizar.md` no se resuelven con valores mágicos en código. Hasta que haya una decisión confirmada:

- usa una opción configurable por campaña o versión de reglas; o
- exige resolución manual en el flujo correspondiente; o
- muestra una alerta bloqueante si no puede existir un cálculo fiable.

---

## 4. Arquitectura objetivo

La estructura concreta puede variar, pero debe respetar estas responsabilidades.

```text
backend/
  src/main/java/.../
    domain/          # Entidades, value objects, políticas y motor puro de reglas.
    application/     # Casos de uso, comandos, consultas y orquestación transaccional.
    infrastructure/  # JPA, migraciones, adaptadores externos, persistencia.
    api/             # REST, DTOs, validación de entrada, mapeadores y manejo de errores.
  src/test/java/.../
    domain/          # Pruebas unitarias deterministas del motor.
    application/     # Pruebas de casos de uso.
    api/             # Pruebas de contrato e integración.

frontend/
  src/
    modules/         # Funcionalidad agrupada por dominio de usuario.
    views/           # Pantallas enrutables.
    components/      # Componentes reutilizables y presentacionales.
    stores/          # Estado de UI y caché de consultas, no reglas de negocio.
    api/             # Cliente HTTP tipado y contratos.
    router/
    types/
```

### 4.1 Arquitectura inicial ya materializada

En este repositorio YA existe una base inicial que debe respetarse al ampliar funcionalidad:

```text
backend/
  src/main/java/com/jadecaravan/
    domain/
      campaign/        # Agregados y reglas nucleares de campaña/caravana.
      calculation/     # Motor puro de cálculo, validaciones y desgloses.
      rules/           # Versiones de reglas, decisiones y catálogos.
    application/
      campaign/
        port/in/       # Casos de uso de entrada del contexto de campaña.
        port/out/      # Puertos hacia persistencia o servicios externos.
        service/       # Orquestación transaccional del contexto de campaña.
    adapter/
      in/web/
        campaign/      # Controladores HTTP del dominio de campaña.
        dto/           # DTOs compartidos o transversales.
        campaign/dto/  # DTOs específicos de campaña.
      out/
        persistence/   # Persistencia, JPA, repositorios y mapeadores.
        system/        # Adaptadores de sistema/entorno.
    config/            # Wiring explícito de Spring.

frontend/
  src/
    modules/
      campaign/
        api/           # Cliente HTTP y contratos del módulo campaña.
        stores/        # Estado de UI del módulo campaña.
        types/         # Tipos del módulo campaña.
        views/         # Pantallas enrutables del módulo campaña.
    views/             # Solo vistas verdaderamente globales.
    components/        # Componentes reutilizables y presentacionales.
    layouts/           # Shells y composiciones de página.
    stores/            # Estado global transversal, no específico de módulo.
    api/               # Infraestructura HTTP compartida.
    router/
    types/             # Tipos globales/transversales.
```

### 4.2 Reglas de colocación de archivos

Aplica estas reglas al crear código nuevo:

- Una pantalla de dominio no nace en `frontend/src/views`; debe vivir en `frontend/src/modules/<dominio>/views`.
- Un store de dominio no nace en `frontend/src/stores`; debe vivir en `frontend/src/modules/<dominio>/stores`.
- `frontend/src/views` queda reservado para vistas globales, shells de navegación o entradas no ligadas a un dominio concreto.
- `frontend/src/stores` queda reservado para estado global transversal de aplicación.
- Los casos de uso de campaña deben vivir bajo `backend/src/main/java/com/jadecaravan/application/campaign/...`.
- Los controladores y DTOs específicos de campaña deben vivir bajo `backend/src/main/java/com/jadecaravan/adapter/in/web/campaign/...`.
- La persistencia del dominio debe vivir bajo `backend/src/main/java/com/jadecaravan/adapter/out/persistence/...`.
- No dupliques la misma responsabilidad en una carpeta genérica y otra de módulo; si una pieza pertenece a un dominio, vive en su módulo.

### Backend

- Java 21.
- Spring Boot.
- Maven.
- Spring Web, Bean Validation, JPA/Hibernate, PostgreSQL y Flyway.
- El dominio debe poder probarse sin levantar Spring ni PostgreSQL.
- Prefiere clases pequeñas, nombres explícitos y value objects a mapas o `String` sin tipo.

### Frontend

- Vue 3 + Vite + TypeScript.
- Vue Router y Pinia.
- Cliente HTTP tipado.
- La interfaz está en español de España.
- Prioriza tablas con desglose, alertas accionables y formularios con validación clara sobre visualizaciones ornamentales.

---

## 5. Convenciones de datos y precisión

Aplica estas reglas de forma estricta:

| Concepto | Representación canónica |
|---|---|
| Moneda | Entero en piezas de cobre (`cp`) |
| Capacidad, ocupación, cantidades fraccionarias | `BigDecimal` |
| Puntos de golpe, puntos de atributo, días, usos | Enteros |
| Temperatura | Fahrenheit canónico; Celsius solo de presentación |
| Día de campaña | Entero secuencial |
| Tiradas | Dado natural, modificadores desglosados, CD, total y resultado narrativo |
| Inventario | Lotes cuando importe origen, caducidad, carro o estado |

No uses `float` ni `double` para capacidades, moneda, porcentajes acumulados o cálculos que puedan afectar a una regla.

Define y prueba explícitamente los redondeos. Cuando una regla no los especifique, remite a una decisión configurable de `RuleSetVersion` en vez de asumirla.

---

## 6. Modelo y estado

Respeta los agregados principales definidos en `01-modelo-de-dominio.md`.

Reglas de implementación importantes:

- Un `Traveller` puede existir aunque no cuente como viajero a efectos de consumo, alojamiento o rol; no infieras estos valores por su nombre o especie.
- Una bestia de tiro no se trata como viajero mientras actúe como tiro, salvo que el modelo o una decisión de campaña indique lo contrario.
- Un carro calcula sus estadísticas efectivas a partir del tipo base y sus mejoras activas.
- Los efectos temporales deben guardar origen, inicio, final, ámbito, apilamiento y versión de reglas.
- Los catálogos son datos versionables; no codifiques las fichas de carros, mejoras, dotes o roles como constantes dispersas en clases.
- El estado actual debe poder derivarse de eventos, operaciones y datos base, incluso si existe una proyección optimizada para consulta.

---

## 7. Motor de cálculo

El comportamiento exacto está en `05-motor-de-calculo.md`. Conserva su orden de evaluación y devuelve resultados explicables.

Cada cálculo relevante debe devolver, como mínimo:

```text
CalculationResult<T>
- value
- breakdown[]       # concepto, valor, origen, regla aplicada
- warnings[]        # incoherencias no bloqueantes
- blockers[]        # condiciones que impiden resolver o cerrar el día
- ruleSetVersionId
```

### Requisitos del motor

- Debe ser determinista para una misma entrada y versión de reglas.
- Debe recibir dependencias explícitas; evita consultar base de datos desde políticas de dominio puras.
- Debe separar: datos base, modificadores, validación, cálculo y efectos resultantes.
- Debe identificar los efectos que se aplicaron y los que se ignoraron por incompatibilidad, límite o falta de requisitos.
- Debe mantener visibles penalizaciones globales aplicadas una sola vez, como el aislamiento de frío actualizado.
- Debe validar los límites antes de cerrar un día: alojamiento, carga, conductor, tiro, roles, recursos consumibles y decisiones manuales obligatorias.

No combines en una única función opaca el cálculo de consumo, velocidad, descontento, clima y cierre diario. Divide por conceptos y compón resultados.

---

## 8. Flujos de día de campaña

El ciclo obligatorio está definido en `03-ciclo-diario.md`.

Estados recomendados:

```text
DRAFT -> PLANNED -> RESOLVING -> CLOSED
                     \-> CANCELLED
```

Condiciones mínimas para cerrar un día:

- no existen bloqueadores sin resolver;
- se han validado alojamiento, tiro, carros operativos, carga y roles;
- se han registrado las tiradas o sus resoluciones manuales cuando correspondan;
- se han aplicado consumo, desgaste, efectos temporales y cambios de inventario;
- se ha creado el resumen reproducible y las entradas de auditoría.

El cierre debe ser transaccional: no dejes un día parcialmente cerrado, con consumo descontado pero sin historial, o con daño aplicado sin efecto de descontento registrado.

---

## 9. API REST

Respeta `06-contrato-api.md`. Al añadir o cambiar endpoints:

- usa DTOs de entrada y salida; nunca expongas entidades JPA directamente;
- valida la entrada con Bean Validation y reglas de aplicación;
- devuelve errores estables, tipados y útiles para la UI;
- incluye desgloses, avisos y bloqueadores cuando el endpoint calcule o valide;
- mantén compatibilidad hacia atrás o versiona de forma explícita si el contrato debe romperse;
- evita endpoints genéricos que permitan modificar cualquier campo de una entidad sin una intención de negocio clara.

Ejemplos de intención correcta:

- `assignTravellerToCart`
- `assignTowingBeast`
- `planCampaignDay`
- `resolveCheck`
- `closeCampaignDay`
- `applyInventoryAdjustment`

Ejemplo que debe evitarse:

- `PATCH /anything/{id}` sin validaciones ni semántica de dominio.

---

## 10. Frontend

Respeta `07-especificacion-frontend.md`.

### Regla principal

El frontend presenta una propuesta calculada por el backend; no duplica el motor de reglas.

### Requisitos de UX

- Mostrar siempre el **desglose** que explique un total importante.
- Distinguir visualmente entre aviso, bloqueo, decisión pendiente y dato histórico.
- No ocultar una operación inválida: explica qué requisito falta y enlaza al lugar donde se corrige.
- Pedir un motivo para ajustes, correcciones y decisiones manuales.
- No permitir edición directa de un día cerrado.
- Los formularios deben anticipar incompatibilidades de carro, carga, mejora, rol o tiro antes de guardar.
- Usar unidades legibles y consistentes: millas/día, PG, Fuerza requerida, provisiones, cp/po y °F/°C.

El frontend puede hacer validaciones de comodidad, pero el backend conserva siempre la validación definitiva.

---

## 11. Pruebas obligatorias

`08-pruebas-de-aceptacion.md` es un contrato de comportamiento mínimo, no una lista opcional.

Antes de dar por terminada una tarea que afecte reglas:

1. añade o adapta pruebas unitarias del motor;
2. añade pruebas de aplicación para la operación o caso de uso;
3. conserva o amplía las pruebas de aceptación afectadas;
4. ejecuta toda la suite relevante;
5. verifica que el cálculo incluye desglose y trazabilidad.

### Reglas de pruebas

- No pruebes solo números finales: prueba también los modificadores y bloqueadores que los producen.
- Prueba límites, incompatibilidades, efectos acumulativos y casos límite.
- Usa datos de prueba legibles: `cartWithIceRunnersOnNonFrozenTerrain`, no `test1`.
- Las pruebas del dominio no deben depender de la hora actual, orden aleatorio ni base de datos.
- Para tiradas, usa una abstracción de dado inyectable o registra un resultado manual; no uses aleatoriedad no controlada en pruebas.

Comandos esperados una vez configurado el repositorio:

```bash
# Backend
cd backend && ./mvnw test

# Frontend
cd frontend && npm run typecheck && npm run test && npm run build
```

Si alguno no existe todavía, crea una alternativa estándar y documenta el comando real en el `README` técnico del módulo.

---

## 12. Migraciones y persistencia

- Todas las modificaciones de esquema se realizan con Flyway.
- No modifiques manualmente una base de datos compartida para simular una migración.
- Las migraciones deben ser aditivas y revisables.
- Usa restricciones de base de datos para invariantes simples, pero conserva la validación de negocio en el dominio/aplicación.
- Añade índices para las consultas frecuentes previstas: campaña, día, carro, viajero, lote de inventario, entradas de historial y versión de reglas.
- Los datos iniciales de catálogo deben ser idempotentes y versionables.

---

## 13. Cambios de reglas y documentación

Cuando una tarea cambie una regla, fórmula, límite, catálogo o interpretación:

1. actualiza el Markdown normativo correspondiente;
2. añade o actualiza la decisión en `10-decidir-antes-de-automatizar.md` si procede;
3. crea o actualiza casos de aceptación;
4. crea una migración de datos si es necesaria;
5. asegura que los días históricos conservan su `RuleSetVersion`.

No cambies una fórmula solo en código. No modifiques una regla de campaña para simplificar la implementación.

---

## 14. Prohibiciones explícitas

No hagas ninguna de estas cosas:

- no inventar reglas de Pathfinder o de campaña para cubrir lagunas;
- no convertir una decisión narrativa en automatismo obligatorio;
- no almacenar totales derivados como fuente de verdad única;
- no usar `double` para valores reglados;
- no ocultar el origen de un modificador;
- no editar silenciosamente el historial;
- no permitir que Vue y Java calculen la misma regla por separado;
- no codificar catálogos de campaña en condicionales dispersos;
- no desbloquear el cierre de día ignorando un requisito incumplido;
- no borrar datos históricos para “arreglar” una inconsistencia;
- no implementar los puntos pendientes de `10-decidir-antes-de-automatizar.md` como hechos definitivos.

---

## 15. Orden de implementación recomendado

Construye en incrementos verticales, siempre con pruebas y una pantalla utilizable:

1. **Base del proyecto**: módulos Java/Vue, PostgreSQL, Flyway, manejo de errores, autenticación si se decide, CI y datos de catálogo mínimos.
2. **Estado de caravana**: campaña, estadísticas, viajeros, carros, bestias e inventario básico.
3. **Motor de validación**: capacidad, alojamiento, conductor, tiro, carros operativos y consumo.
4. **Planificación diaria**: roles, viaje/descanso, clima, recursos y cálculo de resumen previo.
5. **Cierre diario y auditoría**: operaciones, tiradas, efectos, inventario, descontento e inmutabilidad.
6. **Tablero**: alertas, resúmenes y acceso al desglose de cada total.
7. **Sistemas avanzados**: comercio, daños complejos, docencia, enfermedades, asedios y subsistemas opcionales.

No empieces por sistemas avanzados ni por una interfaz estética antes de que el núcleo sea correcto y comprobable.

---

## 16. Criterio de terminación

Una tarea está terminada únicamente cuando:

- implementa la intención descrita sin alterar reglas no relacionadas;
- las restricciones se validan en backend;
- los cálculos son reproducibles y explicables;
- hay historial de cualquier cambio de estado relevante;
- las pruebas afectadas pasan;
- la interfaz muestra errores, avisos y desglose necesarios;
- la documentación está actualizada;
- no queda una ambigüedad nueva escondida en código.

Cuando haya duda, prioriza siempre: **fidelidad a las reglas de campaña, trazabilidad y claridad para el director de juego**.

## 17. Estado actual del proyecto

El repositorio ya dispone de:

- la base de decisión de reglas y auditoría de campaña;
- el catálogo versionado de referencia expuesto por backend en `/api/v1/catalogs/{catalogName}`;
- el `catalogStore` y el `catalogApi` en frontend para consumir los catálogos sin recalcular reglas;
- validación y pruebas automatizadas que cubren la carga de catálogos y la respuesta Problem Details cuando un catálogo no existe.

Este bloque solo refleja el estado técnico vigente para orientar futuras ampliaciones; no sustituye a las reglas anteriores.
