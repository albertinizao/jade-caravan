# 07. Especificación frontend

## 1. Objetivo UX

La aplicación debe permitir al director saber, en menos de un minuto:

1. si la caravana puede salir;
2. qué recurso se agotará primero;
3. qué carros o viajeros están incumpliendo una regla;
4. qué tiradas debe pedir en mesa;
5. qué cambió desde el día anterior.

## 2. Rutas

| Ruta | Pantalla |
|---|---|
| `/` | Selector de campaña. |
| `/campaigns/:id/dashboard` | Vista general. |
| `/campaigns/:id/day/:day` | Planificador y cierre diario. |
| `/campaigns/:id/carts` | Gestión de carros. |
| `/campaigns/:id/travellers` | Viajeros, contratos, roles y relaciones. |
| `/campaigns/:id/inventory` | Carga, provisiones, combustible y tesoro. |
| `/campaigns/:id/beasts` | Bestias, tiro, fatiga y clima. |
| `/campaigns/:id/commerce` | Compras, ventas, museo y crónicas. |
| `/campaigns/:id/history` | Diario y auditoría. |
| `/campaigns/:id/rules` | Dotes, catálogo y decisiones de reglas. |

## 3. Tablero

Tarjetas obligatorias:

- carros operativos / total;
- puntos de golpe de caravana;
- viajeros alojados / capacidad;
- carga ocupada / capacidad;
- consumo de hoy y días de provisiones;
- velocidad prevista;
- descontento, moral y estado de motín;
- bestias libres, asignadas y cercanas a fatiga;
- salarios pendientes.

Alertas ordenadas: `ERROR`, `WARNING`, `INFO`.

### Alertas de error

- pasajero o carga por encima de capacidad;
- carro sin conductor;
- propulsión insuficiente;
- mejora incompatible;
- uso de carga inexistente;
- rol sin requisito;
- motín sin tirada diaria resuelta.

### Alertas de aviso

- capacidad al 90% o más;
- suministros para menos de tres días;
- lote perecedero que degrada hoy;
- bestias que se fatigan hoy o mañana;
- salario mensual vencido;
- pausa civilizada que alcanzará un umbral;
- dote temporal a punto de expirar.

## 4. Gestión de carros

Cada fila debe mostrar:

```text
Nombre | Tipo | PG actual/máximo | Dureza | Tiro actual/requerido |
Pasajeros actual/máximo | Carga actual/máxima | Conductor | Mejoras | Alertas
```

Al abrir detalle:

- pestaña de pasajeros;
- pestaña de carga;
- pestaña de tiro;
- pestaña de mejoras;
- pestaña de daño y reparaciones;
- desglose de beneficios a caravana.

## 5. Planificador diario

Diseño por pasos, no formulario interminable:

1. **Contexto**: viaje, clima, terreno, asentamiento y horas.
2. **Personas**: roles, alojamiento, conductores y tareas especiales.
3. **Bestias**: tiro y descansos.
4. **Recursos**: comida, ayuno, carbón, hielo, reparación, regalos.
5. **Previsión**: velocidad, consumo, alertas y tiradas.
6. **Resolución**: eventos, tiradas y consecuencias.
7. **Cierre**: resumen antes de confirmar.

No mostrar un botón de cierre activo mientras haya errores duros sin excepción firmada por el director.

## 6. Interacciones esenciales

- Arrastrar viajero a carro para alojamiento, con previsualización de ocupación.
- Arrastrar bestia a carro para tiro, con indicador de fuerza y límite.
- Arrastrar lote de carga, con validación del tipo de carro antes de confirmar.
- Selector de rol por día con requisitos y límites visibles.
- Botón “preparar tirada” que abre modal con desglose y entrada de dado manual.
- Botón “usar dado digital” opcional; la tirada nunca se oculta ni se borra.
- Historial de deshacer solo mientras el día está en borrador; después, crear ajuste auditado.

## 7. Accesibilidad y claridad

- No depender solo del color: iconos y texto para alertas.
- Mostrar tanto valor actual como máximo en cada barra.
- Todos los modificadores deben tener tooltip o panel explicativo.
- Los nombres de reglas se mantienen en español de la campaña.
- El frontend no replica fórmulas de negocio críticas: llama al backend para el cálculo final.

## 8. Estado en Vue

Pinia sugerido:

```text
campaignStore
caravanStore
dayPlannerStore
catalogStore
notificationStore
```

Los borradores diarios pueden mantenerse localmente, pero toda validación decisiva debe recibirse del backend antes de cerrar.
