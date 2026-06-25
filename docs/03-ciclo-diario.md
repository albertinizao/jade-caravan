# 03. Ciclo diario de operación

## 1. Propósito

Un día de campaña no es una edición libre de celdas: es una transacción compuesta que prepara, resuelve y registra las consecuencias de una jornada.

## 2. Estados del día

| Estado | Significado |
|---|---|
| `DRAFT` | El director prepara contexto y asignaciones. No cambia recursos. |
| `PLANNED` | Todas las validaciones duras han pasado o han sido aceptadas como excepción. |
| `RESOLVING` | Se registran tiradas, eventos y operaciones. |
| `CLOSED` | Se aplicaron efectos, inventario, contadores y auditoría. |

## 3. Flujo recomendado

### Paso 1. Abrir día

Crear un `CampaignDay` con:

- tipo de actividad;
- terreno y clima;
- temperatura;
- asentamiento, si existe;
- horas de viaje previstas;
- distancia prevista;
- contexto de crisis, campamento o combate.

### Paso 2. Asignar roles y alojamiento

- asignar rol diario a cada viajero relevante;
- seleccionar carro conducido por cada carretero;
- confirmar alojamientos para descanso;
- indicar objetivo de sirvientes, profesores, instructores, artilleros y agricultores;
- elegir acciones de dotes y recursos: ayuno, bendición, celebración, hielo, carbón, etc.

### Paso 3. Validar antes de partir

La app debe bloquear `PLANNED` ante:

- exceso de pasajeros o carga;
- carro sin conductor;
- propulsión insuficiente;
- restricciones de almacenamiento incumplidas;
- rol que supera límite duro;
- requisito de rol no satisfecho;
- dote inactiva usada;
- inventario insuficiente.

Puede permitirse una excepción del director, pero debe pedir motivo y quedar resaltada.

### Paso 4. Calcular previsión

Mostrar sin aplicar cambios:

- velocidad base, modificadores y distancia prevista;
- consumo previsto y provisiones disponibles;
- bonificadores de ataque, CA, seguridad y determinación;
- estado de motín;
- riesgo de fatiga de bestias;
- alertas de caducidad, hielo, salarios y contratos;
- producción prevista de agricultores, batidores, leñadores y neveros.

### Paso 5. Resolver el día

Registrar, según proceda:

- tiradas de seguridad o determinación;
- eventos de clima, peligros, comercio o combate;
- reparación, cuidado de animales o curación;
- producción y consumo;
- elección de huir;
- daños, muertes, pérdidas y descontento;
- transacciones en asentamiento.

La aplicación nunca decide el contenido narrativo del evento; solo ayuda a aplicar la consecuencia seleccionada.

### Paso 6. Cerrar día

El cierre debe ejecutar en un orden determinista:

1. aplicar resultados de eventos y daños;
2. aplicar efectos de huida o falta de avance;
3. calcular y consumir provisiones;
4. aplicar producción de roles y recursos de entorno;
5. degradar perecederos y fundir hielo si procede;
6. actualizar días de fatiga de bestias y recuperación por descanso;
7. aplicar descontento, motín y reducciones;
8. actualizar usos diarios/semanales/mensuales/trimestrales;
9. cerrar registros de comercio, salario y crónicas;
10. generar ledger y resumen del día.

## 4. Operaciones diarias clave

### 4.1 Viajar

Precondiciones mínimas:

- carros, pasajeros y carga dentro de capacidad;
- todos los carros que viajan tienen carretero;
- todos los carros tirados tienen propulsión suficiente;
- hay una decisión explícita sobre consumo y descanso.

Resultado:

- distancia resuelta;
- consumo;
- progreso de fatiga;
- peligros si se han resuelto;
- actualización de ubicación y contador de viaje salvaje.

### 4.2 Descansar

Puede recuperar fatiga, permitir reparación y activar cuidado de animales. Un descanso no se considera pausa civilizada si incluye reparaciones, otros trabajos o imposibilidad de avanzar.

### 4.3 Estar detenido en civilización

Debe indicar si el día fue realmente inactivo. Solo los días inactivos alimentan el contador de pausas en civilización.

### 4.4 Reparar

- Requiere carrero activo, carro objetivo y un día sin movimiento.
- Consume una unidad de material de reparaciones.
- Genera prueba de Seguridad.
- Recupera `15 × nivel` PG en el carro objetivo si la resolución es válida.

### 4.5 Comer, celebrar, regalar y ayunar

Son operaciones independientes, con sus propios ledger entries. La pantalla debe impedir gastar dos veces la misma provisión.

### 4.6 Comercio

La transacción registra origen, destino, lote vendido, tirada, modificadores, ingresos, descuentos, tenderete y diplomáticos.

## 5. Resumen de cierre

Todo día cerrado debe producir una tarjeta o informe con:

```text
Día N - tipo de actividad
- Distancia: prevista / real
- Consumo: previsto / pagado / déficit
- Producción: provisiones, leña, hielo, objetos
- Eventos y comprobaciones
- Daño y reparaciones
- Descontento: antes -> después
- Cambios de inventario
- Alertas que continúan al día siguiente
```
