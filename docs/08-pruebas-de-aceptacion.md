# 08. Pruebas de aceptación

Cada escenario debe convertirse en prueba unitaria del dominio y, cuando corresponda, en prueba de integración API.

## A. Capacidad y transporte

### AT-01 - Exceso de pasajeros bloquea viaje

**Dado** un carro con capacidad 4 y pasajeros por 4.5,  
**cuando** se planifica viaje,  
**entonces** el plan es inválido con `PASSENGER_CAPACITY_EXCEEDED`.

### AT-02 - Ocupación fraccionaria en carruaje familiar

**Dado** un carruaje familiar con dos menores de ocupación 0.5,  
**cuando** se alojan,  
**entonces** consumen una plaza total y el desglose conserva ambos viajeros.

### AT-03 - Carro de mercancías específicas no mezcla tipos

**Dado** un carro con hierro,  
**cuando** se intenta asignar carbón,  
**entonces** se rechaza la operación salvo que ambas cargas compartan el mismo subtipo explícito.

### AT-04 - Carro museo solo acepta tesoro

**Dado** un carro museo,  
**cuando** se intenta asignar suministros,  
**entonces** se devuelve `CARGO_TYPE_NOT_ALLOWED`.

## B. Roles

### AT-05 - Un carretero por carro

**Dado** dos carros operativos y un único carretero asignado,  
**cuando** se valida el plan,  
**entonces** el segundo carro aparece como `MISSING_DRIVER`.

### AT-06 - Héroe no consume el rol ordinario

**Dado** un PJ con Héroe y Guarda,  
**cuando** se calcula el día,  
**entonces** recibe ambos efectos si cumple requisitos y Héroe no ocupa el slot ordinario.

### AT-07 - Límite de Adivino

**Dado** un Adivino activo,  
**cuando** se asigna un segundo Adivino,  
**entonces** se rechaza la asignación.

### AT-08 - Agricultor fuera de carro huerto

**Dado** un viajero agricultor alojado en un carro normal,  
**cuando** se asigna Agricultura,  
**entonces** se marca inválido.

## C. Tiro y bestias

### AT-09 - Fuerza insuficiente

**Dado** un carro que requiere 10 y bestias que suman 9,  
**cuando** se planifica viaje,  
**entonces** ese carro no puede moverse.

### AT-10 - Fatiga a los cinco días

**Dado** tiro entre 100% y menos de 150% de la Fuerza requerida,  
**cuando** se cierran cinco días consecutivos de viaje,  
**entonces** las bestias quedan fatigadas.

### AT-11 - Duplicar Fuerza evita fatiga de tiro

**Dado** Fuerza total igual o superior al 200% requerido,  
**cuando** se cierran veinte días de viaje,  
**entonces** no se añade fatiga por este motivo.

## D. Consumo e inventario

### AT-12 - Batidor no consume

**Dado** un viajero de consumo 1 con rol Batidor activo,  
**cuando** se calcula consumo,  
**entonces** su consumo no se suma.

### AT-13 - Cocinero convierte suministro

**Dado** una unidad de suministros y un cocinero activo,  
**cuando** se consume la unidad,  
**entonces** se obtienen 15 provisiones y el lote de carga se reduce en una unidad.

### AT-14 - Perecedero se degrada

**Dado** un lote perecedero sin protección,  
**cuando** se cierran dos días,  
**entonces** pierde una provisión de valor.

### AT-15 - Nevera elimina plazas

**Dado** un carro con nevera,  
**cuando** se calcula capacidad,  
**entonces** la capacidad de viajeros es cero aunque exista espacio extendido posterior.

## E. Velocidad y mejoras

### AT-16 - Aislamiento de frío actualizado

**Dado** una caravana con uno o varios carros con aislamiento de frío,  
**cuando** se calcula velocidad,  
**entonces** se aplica una sola penalización de -4 millas/día, no -1 por carro.

### AT-17 - Patines fuera de hielo

**Dado** un carro con patines de hielo en terreno no helado,  
**cuando** se calcula propulsión,  
**entonces** la Fuerza requerida se multiplica por cuatro.

### AT-18 - Ruedas mejoradas globales

**Dado** todos los carros operativos con ruedas mejoradas,  
**cuando** se calcula velocidad,  
**entonces** suma +8 millas/día.

## F. Descontento y motín

### AT-19 - Motín en igualdad

**Dado** descontento igual a moral efectiva,  
**cuando** se cierra el día,  
**entonces** existe motín, aunque el penalizador por exceso sea cero.

### AT-20 - Motín por exceso

**Dado** moral 5 y descontento 8,  
**cuando** se calcula una tirada,  
**entonces** incluye -3 por motín.

### AT-21 - Regalo de tesoro

**Dado** que el último regalo de tesoro fue de una unidad,  
**cuando** se regalan dos unidades,  
**entonces** se reduce el descontento en tres y se actualiza la referencia a dos.

## G. Daño y destrucción

### AT-22 - Reparación consume material

**Dado** un carrero, un día inmóvil y una unidad de material,  
**cuando** se resuelve reparación válida,  
**entonces** se consume exactamente una unidad y se recuperan `15 × nivel` PG.

### AT-23 - Destrucción protegida

**Dado** Levantarse de la nada sin usar,  
**cuando** un carro recibiría daño letal,  
**entonces** queda a 0 PG, obtiene el rasgo y no se destruye.

### AT-24 - Segunda destrucción normal

**Dado** Levantarse de la nada ya consumida,  
**cuando** otro carro llega a 0 PG,  
**entonces** se destruye con consecuencias normales.

## H. Auditoría

### AT-25 - Día cerrado es inmutable

**Dado** un día cerrado,  
**cuando** se intenta editar el consumo,  
**entonces** la API rechaza la edición y exige una operación de ajuste auditada.

### AT-26 - Desglose reproducible

**Dado** un resumen diario cerrado,  
**cuando** se recalcula desde su versión de reglas y su historial,  
**entonces** los totales coinciden exactamente.
