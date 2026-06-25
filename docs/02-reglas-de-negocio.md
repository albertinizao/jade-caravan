# 02. Reglas de negocio

## 1. Estadísticas de caravana

### 1.1 Atributos base

| Atributo | Significado | Límite base |
|---|---|---:|
| Ofensiva | Capacidad de infligir daño. | 0-10 |
| Defensiva | Resistencia, CA y reparación. | 0-10 |
| Movilidad | Riesgos físicos y avance. | 0-10 |
| Moral | Lealtad y resistencia mental/espiritual. | 0-10 |

La caravana empieza con `1` en cada atributo y tres puntos adicionales repartibles. Al subir de nivel no aumenta atributos salvo que una dote lo indique.

### 1.2 Estadísticas derivadas

| Comprobación | Base |
|---|---|
| Ataque | `1d20 + Ofensiva + modificadores` |
| Clase de armadura | `1d20 + Defensiva + modificadores` |
| Seguridad | `1d20 + Movilidad + modificadores` |
| Determinación | `1d20 + Moral + modificadores` |

La CA de caravana se resuelve como una tirada cuando sea necesaria; no es una cifra fija independiente.

### 1.3 Nivel y dotes

- El nivel de la caravana iguala el mayor nivel de los PJ que viajen con ella a tiempo completo.
- Cada subida concede una dote.
- Si una dote deja de cumplir su requisito, queda inactiva hasta recuperarlo.
- Tras 30 días de descanso se pueden reasignar cualesquiera dotes.

## 2. Capacidad, carros y movimiento

### 2.1 Capacidad global

```text
capacidadPasajeros = suma(capacidad efectiva de carros)
ocupacionPasajeros = suma(ocupación de viajeros alojados)
capacidadCarga = suma(capacidad efectiva de carros)
ocupacionCarga = suma(capacidad de lotes transportados)
```

Si la ocupación de pasajeros o carga supera la capacidad correspondiente, la caravana no puede avanzar.

### 2.2 Penalización por exceso de carros

La regla personalizada sustituye el límite rígido oficial:

```text
franquiciaDeCarros = 5 + 2 * instancias( Carros adicionales )
excesoDeCarros = max(0, carrosOperativos - franquiciaDeCarros)
penalizadorExceso = -excesoDeCarros a todas las tiradas de caravana
```

Un carro destruido no cuenta como operativo. Un carro dañado sí.

### 2.3 Conductor obligatorio

Cada carro operativo requiere un viajero con el rol `CARRETERO` asignado a ese carro durante el día. Sin conductor, el carro no puede moverse y la caravana no puede realizar un viaje normal salvo que se deje el carro atrás mediante una operación explícita.

### 2.4 Restricciones por tipo de carro

- Esclavista: solo viajeros con rol `ESCLAVO`.
- Prisioneros: solo viajeros con rol `PRISIONERO`.
- Zoológico: no humanoides.
- Familiar: tamaño Pequeño o menor puede contar como `0.5` si el director lo habilita en ese carro.
- Mercancías específicas: un único subtipo de mercancía por carro.
- Suministros: únicamente suministros y suministros perecederos.
- Museo: solo tesoro.
- Huerto: solo viajeros alojados/asignados en ese carro pueden ejercer `AGRICULTOR`.
- Escuela: permite como máximo dos profesores activos.
- Adivino: el rol no está disponible sin un carro de adivino operativo.
- Médico: los pasajeros asignados a este carro duplican los cuidados a largo plazo que reciben.

## 3. Propulsión y bestias de tiro

### 3.1 Fuerza práctica

Las criaturas de cuatro o más patas duplican su Fuerza a efectos de tiro.

```text
fuerzaPractica = fuerzaBase * 2 si tieneCuatroOMasPatas
fuerzaTotalCarro = suma(fuerzaPractica de bestias asignadas)
```

Cada carro define también el máximo de criaturas de cada tamaño que puede arrastrarlo. No se puede superar ese máximo sin una mejora de tiro compatible.

### 3.2 Suficiencia y fatiga

| Fuerza total respecto a requerida | Consecuencia |
|---|---|
| Menor que la requerida | El carro no puede moverse. |
| Desde 100% hasta menos de 150% | Bestias fatigadas tras 5 días consecutivos de tiro. |
| Desde 150% hasta menos de 200% | Fatiga tras 10 días consecutivos. |
| 200% o más | No adquieren fatiga por arrastrar ese carro. |

Un día completo de descanso elimina esta fatiga de tiro.

La aplicación mantiene un contador de días consecutivos por conjunto de bestias asignado a cada carro. Reasignar animales no borra su fatiga individual.

### 3.3 Adaptación térmica de bestias

Cada tipo tiene un ajuste de adaptación. Para evaluar el clima, la app calcula una temperatura efectiva:

```text
temperaturaEfectivaBestiaF = temperaturaAmbienteF - (adaptacionFrio * 10)
```

Una adaptación positiva mejora la tolerancia al frío; una negativa, la tolerancia al calor. La consecuencia mecánica concreta del clima extremo se deja como evento o regla configurable, pues el catálogo solo establece los rangos de adaptación.

## 4. Velocidad y viaje

### 4.1 Velocidad base

Se toma la menor velocidad de las criaturas que tiran de carros:

| Velocidad de la bestia | Millas diarias base |
|---:|---:|
| 20 pies | 8 |
| 30 pies | 16 |
| 40 pies | 24 |
| 50 pies | 32 |
| 60 pies | 40 |

### 4.2 Modificadores de velocidad

Aplicar después de la velocidad base y del modificador de terreno configurado por el director:

- `+4 millas/día` por instancia de `Veloz`, máximo tres.
- `+8 millas/día` si todos los carros operativos poseen ruedas mejoradas.
- `+4 millas/día` si todos poseen tiro de cuatro caballos; los niveles seis y ocho añaden sus propios `+4` cuando proceda.
- `-1 milla/día` por carro acorazado.
- `-4 millas/día` una sola vez si existe al menos un carro con aislamiento para el frío, según la actualización de campaña.
- `-1 milla/día` por aislamiento para el calor, mientras no se dicte una actualización distinta.
- Penalizaciones por motín, fatiga y viaje nocturno.

La interfaz debe mostrar un desglose y no redondear sin informar.

### 4.3 Horas de viaje y descanso

- Un día estándar: 12 horas de viaje y 12 de descanso.
- Viajar tras anochecer reduce a la mitad la velocidad.
- Forzar más de 12 horas fatiga la caravana: `-2` a tiradas y mitad de velocidad base.
- Cada hora adicional exige Seguridad CD 15, aumentando la CD en 1 por prueba sucesiva. Fallar provoca agotamiento: `-6` a tiradas e inmovilidad.
- 12 horas de descanso completo: agotada -> fatigada; fatigada -> normal.

## 5. Consumo, suministros y descanso

### 5.1 Consumo diario

```text
consumoBase = viajerosContablesQueComen + suma(consumo efectivo de carros)
consumoFinal = aplicarDotesYEventos(consumoBase)
```

Los batidores activos no cuentan para el consumo de la caravana durante el día.

Una unidad de `SUMINISTROS` equivale a 10 provisiones. Al desembalarla, se debe registrar un lote de provisiones disponibles. Un cocinero puede convertir una unidad en 15 provisiones; la misma unidad no puede beneficiarse de dos cocineros.

### 5.2 Falta de provisiones

Si no se puede pagar el consumo requerido para descansar:

- se gastan todas las provisiones disponibles;
- la caravana sufre `1d6` de daño y queda fatigada;
- sin provisiones suficientes no se puede reparar ni recuperarse de fatiga;
- mientras siga sin provisiones, sufre `1d6` de daño dos veces al día hasta ser destruida.

### 5.3 Ayuno intermitente

Al inicio del día se puede activar. La app prepara Determinación:

```text
CD = 15 + descontentoActual + díasPreviosDeAyuno
```

Si se resuelve con éxito, el consumo de viajeros se reduce a la mitad y el descontento aumenta en 1. Si no hubo suministros suficientes y el día anterior sí los hubo, se registra aumento de descontento de 3 conforme a la redacción de campaña.

El contador de días previos de ayuno se actualiza al cerrar el día; un día sin ayuno lo reinicia.

### 5.4 Suministros perecederos

- Cada unidad vale inicialmente 10 provisiones.
- Cada dos días pierde una provisión de valor.
- Para regalarse cuentan como suministro solo si el conjunto regalado suma 10 provisiones o más.
- Deben modelarse por lote para no degradar toda la reserva por igual.

### 5.5 Nevera e hielo

La nevera es una mejora de carro:

- elimina permanentemente la capacidad de viajeros de ese carro;
- esa capacidad no puede recuperarse con otras mejoras;
- los perecederos almacenados en ella tardan un día adicional en perder efectividad;
- si se consume hielo, la protección del hielo sobre esos perecederos dura el triple;
- el hielo que se fundiría por calor se pierde a razón de un cuarto de unidad, equivalente a multiplicar por cuatro su duración.

El hielo ordinario permite, por unidad, proteger hasta 10 unidades de suministros perecederos durante dos días. También puede enfriar hasta 10 carros en 10 °F.

### 5.6 Carbón, leña y estufa

- Una unidad de carbón, cuando la caravana no se mueve, eleva 20 °F la temperatura de hasta 10 carros elegidos. Un carro no acumula varias aplicaciones simultáneas.
- Un carro con aislamiento para el frío obtiene 10 °F extra de esa aplicación.
- Cuatro unidades de leña equivalen a una de carbón.
- Una estufa permite que hasta 20 criaturas Medianas o menores, contando una Grande como dos, dupliquen el beneficio del carbón. No se acumula con carros aislados ni con otra estufa.

## 6. Daño, reparación y destrucción

### 6.1 Puntos de golpe

```text
pgCaravanaActual = suma(pgActuales de carros operativos o reparables)
pgCaravanaMaximos = suma(pgMáximos efectivos de carros)
```

La caravana queda destruida a 0 PG o menos.

### 6.2 Reparación

Un carrero, en un día sin desplazamiento, puede realizar una prueba especial de Seguridad y gastar una unidad de material de reparaciones para recuperar `15 × nivel de caravana` PG de un carro.

- Máximo cinco carreros pueden intentarlo por día según el límite de rol.
- Reparaciones eficientes concede `+2` por instancia a la comprobación.
- Refuerzo para carros: `+10 PG máximos`, `-1` capacidad de cargamento, límite uno.
- Carros protegidos: `+2` dureza y `+10 PG máximos` para todos los carros.

### 6.3 Destrucción total

Si la caravana se destruye por un evento:

- cada tripulante recibe `2d10 + 5` PG de daño;
- se destruyen equipo y mejoras de caravana;
- mueren las bestias de tiro;
- los carros pueden repararse posteriormente.

La operación debe requerir confirmación fuerte y producir un informe de consecuencias antes de aplicarse.

### 6.4 Levantarse de la nada

La primera vez que un carro fuese a ser destruido, queda en 0 PG y recibe el rasgo `LEVANTADO_DE_LA_NADA`. Solo puede repararse hasta el 50% de su vida máxima. La dote debe almacenar si ya se consumió su protección global.

## 7. Descontento y motín

### 7.1 Estado

- El descontento nunca puede ser menor de 0.
- Se produce motín cuando `descontento >= moral efectiva`.
- Penalizador de motín: `-1` a todas las tiradas por cada punto de exceso sobre la moral.
- Cada día amotinado: Determinación `CD = 20 + descontento`.
  - Fallo: movilidad a la mitad.
  - Fallo por 5 o más: no se mueve o se mueve en dirección aleatoria; el director registra la opción.

### 7.2 Aumentos por sucesos

Para cada fila de la tabla se usa solo el suceso de mayor gravedad ocurrido ese día. La app prepara la prueba con la CD asociada y aplica el aumento si se falla.

| Categoría | Pérdida 1 / CD 15 | Pérdida 2 / CD 20 | Pérdida 3 / CD 25 |
|---|---|---|---|
| Daño de carro | Carro dañado | Pérdida de un carro | Cada carro adicional perdido |
| Caballos | Pérdida de una bestia | Pérdida de un tiro | Pérdida de todos los tiros |
| Daño a viajeros | Herido no PJ | Muerte de viajero, incluso resucitado | Cada muerto adicional |
| Sin avance por adversidad | Un día | Tercer día | Cada tres días adicionales |
| Pausa en civilización | X días | 2X días | Cada día desde 3X |
| Ganancia de descontento consecutiva | 3 días | 6 días | 10 días |

Cada elemento adicional de la misma situación incrementa la dificultad en 2.

`Pausa en civilización` solo cuenta para días sin actividad, reparación o trabajo. Por cada mes completo en territorio salvaje desde la última civilización, se ignoran los dos primeros días que normalmente contarían como pausa.

| Asentamiento | X |
|---|---:|
| Ermitaño | 1 |
| Poblado | 2 |
| Aldea | 3 |
| Pueblo pequeño o grande | 5 |
| Ciudad pequeña o grande | 6 |
| Metrópolis | 7 |
| Otro plano | 0.5 |

### 7.3 Reducción

Reducen en 1, salvo indicación:

- subida de nivel;
- superar Determinación `CD 20 + descontento` al añadir un carro o descansar un día; `+5` si es en asentamiento;
- duplicar el consumo durante un día;
- regalar más carga a la tripulación que en la ocasión anterior, mínimo una unidad;
- regalar más tesoro que en la ocasión anterior, mínimo una unidad: reduce 3 en lugar de 1.

La app debe guardar el máximo/último regalo de carga y tesoro usado como referencia.

## 8. Huir

Para evitar un combate de caravana:

```text
Seguridad CD = 10 + VD del encuentro
```

Si el encuentro es más rápido, se necesitan éxitos adicionales equivalentes a cuántas veces es inferior la velocidad de la caravana. Huir impide avanzar el resto del día.

## 9. Comercio, salarios y economía

### 9.1 Contratos

Un contratado cobra su coste mensual. Si no cobra:

- suma 3 de descontento permanente hasta abandonar;
- intenta abandonar cuando sea seguro;
- mientras tanto solo ejerce Pasajero.

La aplicación debe generar una alerta de nómina antes del cierre de cada mes de campaña.

### 9.2 Comercio básico

Un comerciante permite gastar una unidad de mercancías adquirida en un asentamiento distinto y realizar una prueba especial de Determinación. El resultado en PO es el ingreso de la venta.

Mercancías locales: penalizador base `-20`, bonificador `+1` por cada 10 millas desde el origen. Tras ver el resultado, el director puede decidir vender o conservar.

### 9.3 Bonificadores comerciales

- Mercado ambulante: `+5` por instancia a pruebas de comercio, máximo tres.
- Maestría mercantil: `+floor(nivelCaravana / 4)`, no más instancias que Mercado ambulante.
- Diplomáticos: hasta dos; uno puede aumentar ventas 10% y otro reducir compras 10%.
- Tenderete: `+5%` a ganancias diarias, incluidas ventas de tesoro.
- Caravana de renombre: `+2` social en el nuevo asentamiento y 10% descuento en servicios contratados desde el día siguiente.
- Oferta gancho: una vez por día, tras vender al menos una mercancía, vender un objeto de tesoro con +10%; segunda instancia llega a +20% y exige dos mercancías vendidas.

### 9.4 Museo, crónicas y pabellón

- Museo: en asentamiento, 1% diario del valor de tesoros exhibibles transportados, limitado al 1% del valor base del asentamiento; 2% con animador itinerante asignado al museo.
- Cronista: acumula valor por día de viaje, eventos, niveles y días de aventura; no puede vender una crónica solapada ya vendida en el mismo lugar.
- Pabellón: desplegado en asentamiento, genera una tirada de ingresos y da +2 moral mientras siga montado.

## 10. Dotes

La lista completa, requisitos, límites y efectos se detalla en [04-catalogos.md](04-catalogos.md#5-dotes-de-caravana). Las dotes con usos limitados deben exponer contadores diarios, semanales, mensuales o trimestrales.
