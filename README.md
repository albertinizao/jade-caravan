# Gestión de la Caravana - Documentación de proyecto

Aplicación web para gestionar una caravana de *Pathfinder 1.ª edición* en la campaña **El Regente de Jade**.

La aplicación no sustituye al director de juego. Calcula restricciones, conserva el estado, prepara comprobaciones, registra decisiones y mantiene una trazabilidad completa de lo ocurrido durante el viaje.

## Objetivo funcional

Centralizar la gestión de:

- viajeros, contratos, alojamiento, roles diarios y vínculos;
- carros, mejoras, daño, reparaciones y capacidades;
- bestias de tiro, propulsión, fatiga y adaptación climática;
- cargamento, suministros, artículos de mejora, combustible, tesoro y comercio;
- estadísticas de caravana, dotes, moral y descontento;
- planificación y cierre de cada día de viaje, descanso o estancia;
- peligros, comprobaciones, combates, huidas, eventos y consecuencias;
- historial inmutable de decisiones, tiradas y cambios de inventario.

## Principio rector

> Todo total mostrado por la interfaz debe ser reproducible a partir de datos base, efectos activos y un historial de operaciones.

No se deben guardar como verdad primaria los totales de consumo, velocidad, capacidad, ataque, seguridad o determinación. Pueden cachearse para rendimiento, pero el motor debe poder recalcularlos desde cero.

## Jerarquía de fuentes

1. **Decisiones del director posteriores**: prevalecen sobre todo lo anterior y deben quedar registradas como una nueva versión de reglas.
2. **`Carros Updated.pdf`**: cambios específicos de esta campaña. Prevalece ante cualquier regla contradictoria.
3. **`Reglas de Caravana.pdf`**: reglamento operativo personalizado de la caravana.
4. **`Guia jugador Regente de jade.pdf`**: referencia oficial usada solo para completar lagunas que los documentos anteriores no modifiquen.
5. **`Caravana Regente de Jade(1).xlsx`**: fotografía del estado actual; aporta datos, no reglas, y solo sirve como semilla observacional.

## Documentos

| Documento | Uso principal |
|---|---|
| [00-fuentes-y-alcance.md](00-fuentes-y-alcance.md) | Alcance, precedencia y límites de automatización. |
| [01-modelo-de-dominio.md](01-modelo-de-dominio.md) | Entidades, relaciones y estados persistentes. |
| [02-reglas-de-negocio.md](02-reglas-de-negocio.md) | Reglas funcionales, restricciones y efectos. |
| [03-ciclo-diario.md](03-ciclo-diario.md) | Flujo de planificación, resolución y cierre del día. |
| [04-catalogos.md](04-catalogos.md) | Catálogos de carros, mejoras, carga, roles y bestias. |
| [05-motor-de-calculo.md](05-motor-de-calculo.md) | Fórmulas, orden de evaluación y pseudocódigo. |
| [06-contrato-api.md](06-contrato-api.md) | Diseño propuesto del API REST del backend Java. |
| [07-especificacion-frontend.md](07-especificacion-frontend.md) | Pantallas, comportamiento y UX de Vue 3. |
| [08-pruebas-de-aceptacion.md](08-pruebas-de-aceptacion.md) | Casos de prueba que Codex debe implementar. |
| [09-estado-inicial.md](09-estado-inicial.md) | Resumen de la fotografía actual extraída del Excel. |
| [10-decidir-antes-de-automatizar.md](10-decidir-antes-de-automatizar.md) | Ambigüedades detectadas y decisiones de implementación. |

## Recomendación técnica

- **Backend**: Java 21, Spring Boot, Maven, Spring Web, Bean Validation, JPA/Hibernate, PostgreSQL y Flyway.
- **Frontend**: Vue 3, Vite, TypeScript, Vue Router, Pinia y un cliente HTTP tipado.
- **Persistencia**: PostgreSQL. No usar una hoja de cálculo como fuente de verdad tras la migración.
- **Precisión**: `BigDecimal` para capacidad y cantidades fraccionarias; enteros para puntos de golpe, puntos de atributo, días y monedas en piezas de cobre.
- **Arquitectura**: dominio y motor de reglas aislados de HTTP, JPA y Vue. Las reglas deben ser comprobables con pruebas unitarias sin arrancar Spring.

Estas decisiones son una propuesta técnica implementable; no son reglas de campaña.

## MVP recomendado

1. Importar o introducir el estado de la caravana.
2. Gestionar viajeros, carros, bestias, carga y roles.
3. Validar capacidad, conductores, tiro y consumo.
4. Planificar y cerrar un día de viaje o descanso.
5. Registrar tiradas y cambios de descontento.
6. Mostrar un tablero de alertas.

El comercio avanzado, las armas de asedio, la docencia y el tratamiento detallado de enfermedades pueden desarrollarse después, pero el modelo de datos debe admitirlos desde el inicio.
