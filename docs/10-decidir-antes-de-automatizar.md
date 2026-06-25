# 10. Decisiones pendientes antes de automatizar

Estas cuestiones no deben esconderse en código. Hasta que el director las confirme, se implementan como opciones configurables o se resuelven manualmente.

## D-01. Redondeo de espacio extendido

La actualización indica `+25% de la capacidad base, mínimo 1` para viajeros y mínimo 2 para carga, pero no define redondeo.

**Propuesta implementable:** `ceil(capacidadBase × 25%)`, respetando el mínimo.  
**Alternativas:** redondear hacia abajo o conservar decimales.

## D-02. Tiro de cuatro, seis y ocho caballos

No queda completamente explícito si el bono de velocidad de los tres niveles se acumula cuando todos los carros poseen una cadena de mejoras hasta ocho.

**Propuesta:** acumular `+4` por cada nivel universal instalado, por ser mejoras encadenadas distintas.  
**Alternativa:** solo aplicar el bono del nivel más alto.

## D-03. Ganancia de descontento por la tabla de sucesos

La tabla proporciona dificultad y tres niveles de pérdida, pero no expresa con total claridad si el aumento de descontento es siempre 1 al fallar o coincide con gravedad 1/2/3.

**Propuesta temporal:** aumento igual a la columna de pérdida fallida.  
**Alternativa oficial heredada:** siempre +1 tras una prueba con CD variable.

No automatizar el incremento definitivo sin elegir una opción.

## D-04. Días consecutivos de ganancia de descontento

La tabla marca 3, 6 y 10 días, pero no explica el reinicio ni el comportamiento tras 10.

**Propuesta:** contador que aumenta solo en días donde el descontento sube; se reinicia en cualquier día sin aumento. Genera una prueba en los umbrales 3, 6, 10 y cada 3 días después de 10 como severidad 3.

## D-05. Estufa y ámbito de su calor

La estufa duplica el beneficio del carbón para hasta 20 criaturas y no se acumula con carros aislados, pero no indica si calienta un conjunto de carros, un campamento o viajeros concretos.

**Propuesta:** acción de campamento dirigida a viajeros, no a carros; incompatible por objetivo con aislamiento frío. Requiere seleccionar hasta 20 unidades de tamaño.

## D-06. Efecto exacto de la nevera con hielo

La regla dice que los perecederos pierden efectividad un día más tarde y que el hielo triplica la duración de sus efectos, pero no define una fórmula única de calendario.

**Propuesta:** sin hielo, degradación cada 3 días dentro de nevera; con hielo, hasta 10 unidades quedan protegidas durante 6 días; tras ello vuelve el ciclo de tres días.

## D-07. Aislamiento para el calor

La actualización cambia explícitamente el aislamiento de frío a -4 millas/día una vez. No modifica el de calor.

**Propuesta:** aplicar -1 milla/día por carro con aislamiento de calor. Confirmar si se quería una penalización global equivalente.

## D-08. Carro taberna

El Excel contiene un `Carro taberna` con estadísticas, pero no hay descripción ni beneficio en las reglas compartidas.

**Propuesta:** crear un `CustomCartType` solo para esta campaña, con las estadísticas observadas y sin beneficio hasta definirlo.

## D-09. Comercio básico

La venta de una mercancía concede PO iguales al resultado de la prueba, pero no detalla si es precio total o beneficio neto.

**Propuesta:** ingreso total; el coste de compra ya quedó reflejado al adquirir el lote. El informe muestra beneficio neto comparando ambos valores.

## D-10. Límites de roles y beneficios combinados

Hay reglas de límite normal `+5`, Viajeros expertos, Héroes, trabajo en equipo, sirvientes y lanzadores de conjuros. El orden exacto no está declarado.

**Propuesta de orden:**

1. sumar roles ordinarios por estadística;
2. aplicar multiplicadores de trabajo en equipo y sirviente sobre los roles objetivo;
3. aplicar límite de roles (`+5 + Viajeros expertos`);
4. añadir Héroe, dotes, carros, equipo y contexto fuera del límite;
5. añadir penalizaciones de exceso de carros, motín y estado.

## D-11. Consumo eficiente y Organización impecable

Ambas dotes reducen consumo y pueden adquirirse varias veces.

**Propuesta:** aplicar reducciones aditivas, respetando el suelo indicado por Consumo eficiente. Organización impecable reduce una unidad por carro y por instancia después de diez días completos con descontento cero.

## D-12. Celebraciones sucesivas

El bono temporal de Moral no se acumula, pero la regla no indica si una nueva celebración sustituye una anterior menor.

**Propuesta:** conservar el mayor bono activo; si el nuevo es igual o mayor, renovar su duración a cinco días. Si es menor, solo reducir descontento sin reemplazar el bono.

## D-13. Reglas oficiales de destrucción frente a las personalizadas

La guía oficial presenta otro daño a tripulantes tras destrucción. El reglamento personalizado indica `2d10+5` a todos los tripulantes.

**Decisión aplicada en esta documentación:** usar `2d10+5` por tener prioridad el reglamento personalizado.

## D-14. Autorización de reglas de esclavitud

El reglamento incluye roles y dotes de esclavitud. Deben modelarse como subsistema opcional, apagado por defecto en nuevas campañas, sin eliminar los datos ni alterar la fidelidad de las reglas si se activa.
