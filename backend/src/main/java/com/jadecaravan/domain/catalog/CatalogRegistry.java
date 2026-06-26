package com.jadecaravan.domain.catalog;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class CatalogRegistry {

    public static final String VERSION_ID = "catalogs-v1";

    private final Map<CatalogName, CatalogDocument<? extends CatalogEntry>> documents;

    private CatalogRegistry(Map<CatalogName, CatalogDocument<? extends CatalogEntry>> documents) {
        this.documents = Map.copyOf(documents);
    }

    public static CatalogRegistry seeded() {
        EnumMap<CatalogName, CatalogDocument<? extends CatalogEntry>> documents = new EnumMap<>(CatalogName.class);
        documents.put(CatalogName.CART_TYPES, new CatalogDocument<>(
                CatalogName.CART_TYPES,
                VERSION_ID,
                CatalogName.CART_TYPES.title(),
                CatalogName.CART_TYPES.description(),
                true,
                List.of(
                        cart("ARCANE_CART", "Carro arcano", CartCategory.SPECIAL, "500 po", 50000, 50, 5, 8, "2 / 8", 2, 2, 2, List.of("máximo 1"), List.of("componentes -10 po", "concentración +4 una vez/día por lanzador"), false, source("docs/04-catalogos.md", "1.3")),
                        cart("DIVINER_CART", "Carro de adivino", CartCategory.TRAVELLER, "400 po", 40000, 60, 5, 5, "1 / 4", 1, 1, 4, List.of("máximo 1"), List.of("habilita Adivino"), false, source("docs/04-catalogos.md", "1.2")),
                        cart("COVERED_CART", "Carro cubierto", CartCategory.TRAVELLER, "150 po", 15000, 30, 5, 4, "1 grande / 4 medianas", 2, 8, 4, List.of(), List.of(), false, source("docs/04-catalogos.md", "1.1")),
                        cart("TRAVELLER_CART", "Carro de viajeros", CartCategory.TRAVELLER, "200 po", 20000, 60, 5, 10, "2 / 8", 2, 8, 4, List.of(), List.of("+1 CA"), false, source("docs/04-catalogos.md", "1.1")),
                        cart("SLAVER_CART", "Carro de esclavista", CartCategory.TRAVELLER, "150 po", 15000, 30, 5, 5, "1 / 4", 2, 16, 2, List.of("máximo 2", "solo esclavos"), List.of(), false, source("docs/04-catalogos.md", "1.1")),
                        cart("PRISONER_CART", "Carro de prisioneros", CartCategory.TRAVELLER, "300 po", 30000, 60, 5, 5, "1 / 4", 2, 10, 2, List.of("máximo 2", "solo prisioneros"), List.of("+2 Seguridad"), false, source("docs/04-catalogos.md", "1.1")),
                        cart("ZOO_CART", "Carro zoológico", CartCategory.TRAVELLER, "175 po", 17500, 60, 5, 5, "1 / 4", 2, 10, 2, List.of("máximo 2", "sin humanoides"), List.of(), false, source("docs/04-catalogos.md", "1.1")),
                        cart("COMFORT_CARRIAGE", "Carruaje cómodo", CartCategory.TRAVELLER, "250 po", 25000, 60, 5, 6, "1 / 4", 1, 4, 4, List.of(), List.of(), false, source("docs/04-catalogos.md", "1.1")),
                        cart("FAMILY_CARRIAGE", "Carruaje familiar", CartCategory.TRAVELLER, "150 po", 15000, 60, 5, 10, "2 / 8", 2, 6, 4, List.of("1 por cada 5 carros", "pequeños a 0.5"), List.of(), false, source("docs/04-catalogos.md", "1.1")),
                        cart("ROYAL_CARRIAGE", "Carruaje real", CartCategory.TRAVELLER, "775 po", 77500, 80, 5, 8, "2 / 8", 2, 3, 2, List.of("máximo 1"), List.of("+2 Determinación por pasajero alojado"), false, source("docs/04-catalogos.md", "1.1")),
                        cart("SUPPLY_CART", "Carro de suministros", CartCategory.CARGO, "300 po", 30000, 60, 5, 10, "2 / 8", 1, 1, 20, List.of("solo suministros y perecederos"), List.of(), false, source("docs/04-catalogos.md", "1.2")),
                        cart("MUSEUM_CART", "Carro museo", CartCategory.CARGO, "350 po", 35000, 60, 5, 5, "1 / 4", 1, 1, 6, List.of("máximo 1", "solo tesoro", "ingresos en asentamiento"), List.of(), false, source("docs/04-catalogos.md", "1.2")),
                        cart("WORKSHOP_CART", "Carro con taller", CartCategory.SPECIAL, "500 po", 50000, 60, 5, 8, "2 / 8", 1, 2, 4, List.of(), List.of("artesanía durante viaje"), false, source("docs/04-catalogos.md", "1.3")),
                        cart("SCHOOL_CART", "Carro escuela", CartCategory.SPECIAL, "375 po", 37500, 60, 5, 8, "2 / 8", 2, 2, 4, List.of("máximo 1", "hasta dos profesores"), List.of(), false, source("docs/04-catalogos.md", "1.3")),
                        cart("GARDEN_CART", "Carro huerto", CartCategory.SPECIAL, "300 po", 30000, 60, 5, 10, "2 / 8", 1, 2, 1, List.of("solo alojados pueden ser agricultores"), List.of(), false, source("docs/04-catalogos.md", "1.3")),
                        cart("MEDICAL_CART", "Carro médico", CartCategory.SPECIAL, "300 po", 30000, 40, 5, 10, "2 / 8", 2, 6, 1, List.of("pasajeros alojados duplican cuidados largos"), List.of(), false, source("docs/04-catalogos.md", "1.3")),
                        cart("EMPTY_CART", "Carro vacío", CartCategory.SPECIAL, "200 po", 20000, 60, 5, 10, "2 / 8", 2, 3, 5, List.of("sin beneficio definido"), List.of(), false, source("docs/04-catalogos.md", "1.3")),
                        cart("PASSENGER_SLED", "Trineo de pasajeros", CartCategory.TRAVELLER, "10 po", 1000, 10, 3, 4, "2 medianas", 2, 2, 1, List.of(), List.of("actualización de campaña"), true, source("docs/04-catalogos.md", "1.1")),
                        cart("CARGO_SLED", "Trineo de carga", CartCategory.CARGO, "10 po", 1000, 10, 3, 4, "2 medianas", 2, 1, 2, List.of(), List.of("actualización de campaña"), true, source("docs/04-catalogos.md", "1.1")),
                        cart("TAVERN_CART", "Carro taberna", CartCategory.SPECIAL, "pendiente", (BigDecimal) null, 60, 5, 10, "2 / 8", 2, 3, 5, List.of("catálogo personalizado de campaña", "sin regla fuente definitiva"), List.of("sin beneficio definido"), true, source("docs/04-catalogos.md", "1.1 + docs/09-estado-inicial.md"), "Entrada personalizada hasta resolver D-08."))));

        documents.put(CatalogName.UPGRADES, new CatalogDocument<>(
                CatalogName.UPGRADES,
                VERSION_ID,
                CatalogName.UPGRADES.title(),
                CatalogName.UPGRADES.description(),
                true,
                List.of(
                        upgrade("ARMOURED", "Acorazado", "x10", null, "1 por carro", "no se apila", List.of(), "PG x1.5, dureza +5, Fuerza x2, mejora un beneficio, -1 milla/día", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("REINFORCEMENT", "Refuerzo", "500 po", 50000, "1 por carro", "no se apila", List.of(), "+10 PG, -1 carga máxima", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("HERALDRY", "Heráldica", "x0.5", null, "1 por carro", "si todos la tienen", List.of(), "si todos la tienen: +2 Moral y -1 descontento por evento social exitoso", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("MAGICAL_LIGHTING", "Iluminación mágica", "600 po", 60000, "1 por carro", "no se apila", List.of(), "luz mágica 60 pies; descuento por antorchas siempreardientes", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("EXTENDED_SPACE_TRAVELLERS", "Espacio extendido - viajeros", "2.000 po", 200000, "repetible", "ceiling(capacidadBase × 25%), mínimo +1", List.of(), "+25% de capacidad base de viajeros, mínimo +1; +2 Fuerza", false, source("docs/04-catalogos.md", "2"), "El redondeo sigue la decisión pendiente D-01."),
                        upgrade("EXTENDED_SPACE_CARGO", "Espacio extendido - cargamento", "2.000 po", 200000, "repetible", "ceiling(capacidadBase × 25%), mínimo +2", List.of(), "+25% de capacidad base de carga, mínimo +2; +2 Fuerza", false, source("docs/04-catalogos.md", "2"), "El redondeo sigue la decisión pendiente D-01."),
                        upgrade("FRIDGE", "Nevera", "x2", null, "1 por carro", "no se apila", List.of(), "dureza +2, viajeros a 0 definitivamente, conserva perecederos e interactúa con hielo", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("ICE_RUNNERS", "Patines de hielo", "x0.1", null, "incompatible con ruedas", "no se apila", List.of("ruedas mejoradas"), "en hielo: Fuerza total necesaria /2; fuera: x4", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("COLD_INSULATION", "Aislamiento frío", "x2", null, "1 por carro", "penalización global única", List.of(), "dureza +2, +20 °F dentro, +10 °F adicional con carbón, -4 millas/día una vez si existe alguno", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("HEAT_INSULATION", "Aislamiento calor", "x2", null, "1 por carro", "penalización global única", List.of(), "dureza +2, -20 °F dentro, -1 milla/día", false, source("docs/04-catalogos.md", "2"), "La penalización exacta sigue la decisión pendiente D-07."),
                        upgrade("LIGHT_BALLISTA", "Balista ligera", "500 po", 50000, "1 por carro", "no se apila", List.of(), "+2 Fuerza; 1 artillero; 2d6+12, alcance 120, carga 2", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("HEAVY_BALLISTA", "Balista pesada", "500 po", 50000, "1 por carro", "no se apila", List.of(), "+6 Fuerza; 3 viajeros; 2d6+20, alcance 180, carga 3", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("CANNON", "Cañón", "6.000 po", 600000, "1 por carro", "no se apila", List.of(), "+2 Fuerza; 2 viajeros; 3d6+20, alcance 100, carga 3", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("DEMON_MOUTH_CANNON", "Cañón boca demoníaca", "9.000 po", 900000, "1 por carro", "no se apila", List.of(), "+6 Fuerza; 2 viajeros; 3d6+20, alcance 100, carga 3", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("DRAGON_FIRE", "Fuego de draco", "4.000 po", 400000, "1 por carro", "no se apila", List.of(), "+6 Fuerza; 2 viajeros; 6d6 cono 60, Ref CD 15, carga 5", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("IMPROVED_WHEELS", "Ruedas mejoradas", "500 po", 50000, "1 por carro", "si todos las tienen", List.of(), "si todos las tienen, +8 millas/día", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("TWO_HORSE_TRAIN", "Tiro de dos caballos", "20 po", 2000, "base 1 grande / 4 medianas", "encadenado", List.of(), "+5 Fuerza, +1 grande / +4 medianas, consumo +1", false, source("docs/04-catalogos.md", "2"), null),
                        upgrade("FOUR_HORSE_TRAIN", "Tiro de cuatro caballos", "100 po", 10000, "requiere tiro cuatro", "encadenado", List.of(), "+10 Fuerza, +2 grandes / +8 medianas, +2 consumo; si todos, +4 millas/día", false, source("docs/04-catalogos.md", "2"), "La suma de los bonos de tiro sigue la decisión pendiente D-02."),
                        upgrade("SIX_HORSE_TRAIN", "Tiro de seis caballos", "150 po", 15000, "requiere tiro cuatro", "encadenado", List.of(), "+10 Fuerza, +2 grandes / +8 medianas, +2 consumo; si todos, +4 millas/día", false, source("docs/04-catalogos.md", "2"), "La suma de los bonos de tiro sigue la decisión pendiente D-02."),
                        upgrade("EIGHT_HORSE_TRAIN", "Tiro de ocho caballos", "200 po", 20000, "requiere tiro seis", "encadenado", List.of(), "+10 Fuerza, +2 grandes / +8 medianas, +2 consumo; si todos, +4 millas/día", false, source("docs/04-catalogos.md", "2"), "La suma de los bonos de tiro sigue la decisión pendiente D-02."))));

        documents.put(CatalogName.CARGO, new CatalogDocument<>(
                CatalogName.CARGO,
                VERSION_ID,
                CatalogName.CARGO.title(),
                CatalogName.CARGO.description(),
                true,
                List.of(
                        cargo("ALTAR", "Altar sagrado", "500 po", 50000, "1", "1", null, "consagrado a una deidad; habilita Caravana santificada", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("PORTABLE_KITCHEN", "Cocina portátil", "500 po", 50000, "1", "1", null, "un cocinero duplica su rendimiento", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("COLD_EQUIPMENT", "Equipo de frío", "200 po", 20000, "1", "1", null, "ignora penalizadores extra de Seguridad/Determinación por frío extremo", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("PAVILION", "Pabellón", "1.200 po", 120000, "5", "5", null, "ingresos en asentamiento y +2 Moral desplegado", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("COMMUNICATION_STONE", "Piedra de comunicación", "3.000 po", 300000, "1", "1", null, "comunicación entre piedras a una milla", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("STALL", "Tenderete", "250 po", 25000, "2", "2", null, "+5% ganancias diarias", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("SURVIVAL_TENT", "Tienda de supervivencia", "150 po", 15000, "1", "1", null, "hasta 10 viajeros si se pierde un carro", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("CAMP_TRAPS", "Trampas de campamento", "1.000 po", 100000, "2", "2", null, "+4 Seguridad para evitar sorpresa al acampar", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("MERCHANDISE", "Mercancías", "10 po", 1000, "1", "1", null, "comerciables en otro asentamiento", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("MERCHANDISE_SPECIFIC", "Mercancías específicas", "variable", null, "1", "1", null, "subtipo y precio propios", "subtipo obligatorio", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("MAGICAL_MATERIALS", "Materiales mágicos", "250 po*", 25000, "1", "1", null, "componentes / creación; actualización eleva a 25.000 po", "valor de subtipo o inventario especial", false, source("docs/04-catalogos.md", "3"), "La valoración actual depende de la actualización de campaña."),
                        cargo("LOCAL_MERCHANDISE", "Mercancías locales", "20 po", 2000, "1", "1", null, "-20 +1/10 millas al vender", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("SUPPLIES", "Suministros", "5 po", 500, "1", "10 provisiones", null, "10 provisiones", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("PERISHABLES", "Perecederos", "1 po", 100, "1", "10 provisiones", "degradan cada dos días", "requiere seguimiento por lote", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("TREASURE", "Tesoro", "-", null, "variable", "50 lb por unidad como guía", null, "valor variable según la pieza", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("CHARCOAL", "Carbón", "1 po", 100, "1", "1", null, "calor para hasta 10 carros", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("ICE", "Hielo", "1 po", 100, "1", "1", null, "frío, protección de perecederos y fusión por calor", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("FIREWOOD", "Leña", "5 pp", 500, "1", "1", null, "cuatro unidades equivalen a carbón", "", false, source("docs/04-catalogos.md", "3"), null),
                        cargo("REPAIR_MATERIAL", "Material de reparaciones", "25 po", 2500, "1", "1", null, "consume una unidad por reparación", "", false, source("docs/04-catalogos.md", "3"), null))));

        documents.put(CatalogName.ROLES, new CatalogDocument<>(
                CatalogName.ROLES,
                VERSION_ID,
                CatalogName.ROLES.title(),
                CatalogName.ROLES.description(),
                true,
                List.of(
                        role("DIVINER", "Adivino", "1", "conjuros de adivinación + carro", "-2 Seguridad/Determinación si falta; consejo semanal con repetición", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("FARMER", "Agricultor", "-", "profesión relevante + carro huerto", "1 suministro cada 2 días", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("ITINERANT_ENTERTAINER", "Animador itinerante", "-", "Interpretar 1", "cobres por población; mejora museo", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("CRAFTSMAN", "Artesano", "-", "Artesanía 1", "artesanía durante aventura; taller permite en viaje", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("GUNNER", "Artillero", "-", "competencia asedio", "maneja solo una máquina de asedio", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("SCOUT", "Batidor", "3", "Supervivencia 1", "2 provisiones o +1 Seguridad; no consume", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("APOTHECARY", "Boticario", "-", "Naturaleza/Alquimia 1", "crea 5 po/día; +50% cuidados a 5 viajeros", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("CARTER", "Carrero", "5", "Carpintería/Ingeniería 1", "repara carros en día inmóvil", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("WAGONER", "Carretero", "uno por carro", "Profesión/Trato animales 1", "permite mover carro", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("COOK", "Cocinero", "5 + expertos", "profesión culinaria", "1 suministro -> 15 provisiones", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("COMEDIAN", "Comediante", "-", "Interpretar 1", "+1 Determinación y mejora reducción descontento", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("MERCHANT", "Comerciante", "5", "Diplomacia/Engañar/Mercader 1", "venta de mercancías", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("CHRONICLER", "Cronista", "-", "Historia/Escritura/Escriba 1", "crea crónica vendible", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("DIPLOMAT", "Diplomático", "2", "Tasación 1", "+10% venta o -10% compra", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("HANDLER", "Domador", "-", "Trato animales 1", "éxitos de adiestramiento, +2 Determinación animal", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("SUPPLY_OFFICER", "Encargado suministros", "-", "Ingeniería 1", "+10% capacidad de bienes por persona", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("SLAVE", "Esclavo", "-", "forzado", "funciona como sirviente; riesgo huida y descontento", true, false, source("docs/04-catalogos.md", "4"), null),
                        role("SLAVER", "Esclavista", "-", "Intimidar/Percepción 1", "controla hasta 10 esclavos con límite DG", true, false, source("docs/04-catalogos.md", "4"), null),
                        role("NIGHT_SCOUT", "Explorador nocturno", "-", "visión oscura o Percepción 1", "chequeo de alarma nocturna", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("GUARD", "Guarda", "-", "AB +1", "+1 Ofensiva y +1 Seguridad contra sorpresa", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("GUIDE", "Guía", "-", "Geografía 1", "+1 Seguridad", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("HERO", "Héroe", "+4 total", "PJ", "+1 Seguridad/Determinación; no ocupa rol ordinario", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("INSTRUCTOR", "Instructor", "-", "conocer idioma", "enseña idioma en 1 o 3 meses", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("SPELLCASTER", "Lanzador de conjuros", "5", "lanzar conjuros", "puede justificar otros roles; bonifica apilable", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("LEADER", "Líder", "1", "Liderazgo", "bonificadores, discursos y acciones de combate", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("METEOROLOGIST", "Meteorólogo", "1", "Naturaleza 3 / conjuro clima", "previsión y +2 Seguridad si acierta", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("PASSENGER", "Pasajero", "-", "-", "sin beneficio; puede pagar viaje", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("PRISONER", "Prisionero", "-", "-", "puede huir; puede asumir culpa de descontento", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("TEACHER", "Profesor", "2 con escuela", "5 rangos habilidad", "puntos de enseñanza mensuales", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("HEALER", "Sanador", "-", "Curar 1", "cuidados largos a seis viajeros", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("SERVANT", "Sirviente", "-", "-", "ayuda a un señor; mejora efectividad de su rol", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("LUMBERJACK", "Leñador", "-", "Supervivencia/Profesión 1", "hasta una leña al día si hay madera", false, false, source("docs/04-catalogos.md", "4"), null),
                        role("ICE_HARVESTER", "Nevero", "1 + 1/10°F bajo 32", "Supervivencia/Profesión 1", "una unidad de hielo al día", false, false, source("docs/04-catalogos.md", "4"), null))));

        documents.put(CatalogName.FEATS, new CatalogDocument<>(
                CatalogName.FEATS,
                VERSION_ID,
                CatalogName.FEATS.title(),
                CatalogName.FEATS.description(),
                true,
                List.of(
                        feat("COLLABORATIVE_CRAFTSMEN", "Artesanos colaborativos", "-", "todos ganan Artesanía colaborativa", "permanente", "no apila", "mientras viajen", false, source("docs/04-catalogos.md", "5"), null),
                        feat("EXTREME_AUTONOMY", "Autonomía extrema", "Seguridad 6", "batidores/agricultores +50%; inmunidad a desabastecimiento 3 días", "1 uso/3 meses", "no apila", "duración limitada", false, source("docs/04-catalogos.md", "5"), null),
                        feat("INTERMITTENT_FAST", "Ayuno intermitente", "-", "prueba para mitad consumo de viajeros", "diario", "no apila", "diario", false, source("docs/04-catalogos.md", "5"), null),
                        feat("PATH_BLESSING", "Bendición del camino", "Santificada", "hechizo divino de nivel 1 a todos", "diario", "no apila", "diario", false, source("docs/04-catalogos.md", "5"), null),
                        feat("LUCKY_CARAVAN", "Caravana afortunada", "carro adivino", "duplica usos semanales del adivino", "requiere carro", "no apila", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("ARMED_CARAVAN", "Caravana armada", "Ofensiva 5", "BAB mínimo de clérigo del nivel de caravana", "persistente", "no apila", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("IMPROVED_CARAVAN", "Caravana mejorada", "nivel 2", "+1 a dos atributos base", "repetible", "apilable", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("BLESSED_CARAVAN", "Caravana bendecida", "Santificada, Determinación 10", "clérigos de una deidad lanzan nivel 1 o 2 gratis", "diaria", "no apila", "diaria", false, source("docs/04-catalogos.md", "5"), null),
                        feat("SANCTIFIED_CARAVAN", "Caravana santificada", "Determinación 5 + altar", "+3 nivel lanzador divino de deidad elegida", "repetible por deidad distinta", "apilable por deidad", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("RENOWNED_CARAVAN", "Caravana de renombre", "Moral 5", "reputación, +2 social y -10% servicios", "al llegar", "persistente", "desde el día siguiente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("FAMILY_CARAVAN", "Caravana familiar", "-", "+1 Moral por vínculo familiar/romance, doble en carruaje familiar", "relaciones activas", "apilable", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("SCAVENGERS", "Carroñeros", "-", "Seguridad para obtener materiales reparación", "semanal", "no apila", "semanal", false, source("docs/04-catalogos.md", "5"), null),
                        feat("ADDITIONAL_CARTS", "Carros adicionales", "-", "+2 carros sin penalización por instancia", "repetible", "apilable", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("CARROS_IN_CIRCLE", "Carros en círculo", "Defensiva 3", "+4 CA, inmóvil, cobertura", "durante ataque", "no apila", "durante ataque", false, source("docs/04-catalogos.md", "5"), null),
                        feat("PROTECTED_CARTS", "Carros protegidos", "Defensiva 6", "+2 dureza, +10 PG a todos", "persistente", "no apila", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("CELEBRATION", "Celebración", "Determinación 5", "consume doble, reduce descontento y da Moral temporal", "5 días", "no apila", "5 días", false, source("docs/04-catalogos.md", "5"), "La sustitución entre celebraciones sigue la decisión pendiente D-12."),
                        feat("ANIMAL_CARE", "Cuidado de animales", "Seguridad 3", "Seguridad + suministro para curar 15×nivel PG", "día de descanso", "no apila", "día de descanso", false, source("docs/04-catalogos.md", "5"), null),
                        feat("EFFICIENT_CONSUMPTION", "Consumo eficiente", "-", "-2 consumo, sin bajar del consumo de carros", "máximo 3", "limitado", "persistente", false, source("docs/04-catalogos.md", "5"), "La pila de reducciones se mantiene explícita hasta resolver D-11."),
                        feat("EMERGENCY_PLANNING", "Planificación de emergencia", "Determinación 4", "repetir Seguridad/Determinación fallida en crisis", "semanal", "no apila", "semanal", false, source("docs/04-catalogos.md", "5"), null),
                        feat("EFFICIENT_REPAIRS", "Reparaciones eficientes", "Defensiva 3", "+2 a reparación", "máximo 3", "apilable", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("FORTUNE_RITUALS", "Rituales de fortuna", "Adivino + Afortunada", "+1 Moral al evitar un fallo con Afortunada", "por uso", "no apila", "por uso", false, source("docs/04-catalogos.md", "5"), null),
                        feat("KNOWN_ROUTES", "Rutas conocidas", "Movilidad 5", "+2 Seguridad y repetición de encuentro indeseado", "regiones exploradas", "apilable por región", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("BRAVE_TRIP", "Tripulación valerosa", "Moral 3", "+2 Determinación contra miedo", "máximo 3", "apilable", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("TEAMWORK", "Trabajo en equipo", "Moral 3", "+25% por compañero en mismo rol, hasta 3", "por rol/día", "no apila", "por rol/día", false, source("docs/04-catalogos.md", "5"), "El orden exacto de aplicación sigue la decisión pendiente D-10."),
                        feat("SLAVE_TRADE", "Venta de esclavos", "Esclavistas", "comprar 75%, vender 200%", "hasta 5 acciones/día/esclavista", "limitado", "persistente", true, source("docs/04-catalogos.md", "5"), null),
                        feat("FAST", "Veloz", "Movilidad 5", "+4 millas/día", "máximo 3", "apilable", "persistente", false, source("docs/04-catalogos.md", "5"), null),
                        feat("EXPERT_TRAVELLERS", "Viajeros expertos", "Moral 5", "+1 al tope de bonificadores de roles", "máximo 3", "apilable", "persistente", false, source("docs/04-catalogos.md", "5"), null))));

        documents.put(CatalogName.BEASTS, new CatalogDocument<>(
                CatalogName.BEASTS,
                VERSION_ID,
                CatalogName.BEASTS.title(),
                CatalogName.BEASTS.description(),
                true,
                List.of(
                        beast("LIGHT_HORSE", "Caballo ligero", "75 / 110 po", 7500, "75 / 110 po", 11000, 3, "Grande", 50, 0, "neutral", false, source("docs/04-catalogos.md", "6"), null),
                        beast("HEAVY_HORSE", "Caballo pesado", "200 / 300 po", 20000, "200 / 300 po", 30000, 5, "Grande", 50, 0, "neutral", false, source("docs/04-catalogos.md", "6"), null),
                        beast("PONY", "Poni", "30 / 45 po", 3000, "30 / 45 po", 4500, 2, "Mediano", 40, 0, "neutral", false, source("docs/04-catalogos.md", "6"), null),
                        beast("YAKUTO_HORSE", "Caballo yakuto", "400 / 600 po", 40000, "400 / 600 po", 60000, 5, "Grande", 50, 2, "+2 adaptación al frío", false, source("docs/04-catalogos.md", "6"), null),
                        beast("POTTOKA_HORSE", "Caballo pottoka", "60 / 90 po", 6000, "60 / 90 po", 9000, 2, "Mediano", 40, 1, "+1 adaptación", false, source("docs/04-catalogos.md", "6"), null),
                        beast("SHIRE_HORSE", "Caballo shire", "400 / 600 po", 40000, "400 / 600 po", 60000, 7, "Grande", 40, 0, "neutral", false, source("docs/04-catalogos.md", "6"), null),
                        beast("CAMEL", "Camello", "150 / 225 po", 15000, "150 / 225 po", 22500, 4, "Grande", 50, -3, "-3 adaptación", false, source("docs/04-catalogos.md", "6"), null),
                        beast("MULE", "Mula", "8 / - po", 800, "8 / - po", null, 2, "Mediano", 30, 0, "neutral", false, source("docs/04-catalogos.md", "6"), null),
                        beast("OX", "Buey", "50 / - po", 5000, "50 / - po", null, 6, "Grande", 40, 1, "+1 adaptación", false, source("docs/04-catalogos.md", "6"), null),
                        beast("YAK", "Yak", "100 / - po", 10000, "100 / - po", null, 7, "Grande", 30, 3, "+3 adaptación al frío", false, source("docs/04-catalogos.md", "6"), null),
                        beast("BISON", "Bisonte", "50 / 75 po", 5000, "50 / 75 po", 7500, 8, "Grande", 40, 1, "+1 adaptación", false, source("docs/04-catalogos.md", "6"), null),
                        beast("GIANT_MONITOR_LIZARD", "Lagarto monitor gigante", "300 / 450 po", 30000, "300 / 450 po", 45000, 3, "Enorme", 30, -4, "-4 adaptación", false, source("docs/04-catalogos.md", "6"), null),
                        beast("GRIFFON", "Grifo", "- / 8.000 po", null, "- / 8.000 po", 800000, 3, "Grande", 30, null, "sin adaptación declarada", false, source("docs/04-catalogos.md", "6"), null),
                        beast("GIANT_ELK", "Alce gigante", "200 / 300 po", 20000, "200 / 300 po", 30000, 4, "Grande", 40, 2, "+2 adaptación", false, source("docs/04-catalogos.md", "6"), null),
                        beast("RIDING_DOG", "Perro de monta", "- / 150 po", null, "- / 150 po", 15000, 2, "Mediano", 40, 0, "neutral", false, source("docs/04-catalogos.md", "6"), null),
                        beast("SLED_DOG", "Perro de trineo", "150 / 200 po", 15000, "150 / 200 po", 20000, 3, "Mediano", 40, 3, "+3 adaptación al frío", false, source("docs/04-catalogos.md", "6"), null))));

        return new CatalogRegistry(documents);
    }

    public CatalogDocument<? extends CatalogEntry> catalog(CatalogName name) {
        Objects.requireNonNull(name, "name must not be null");
        CatalogDocument<? extends CatalogEntry> document = documents.get(name);
        if (document == null) {
            throw new IllegalArgumentException("Unknown catalog: " + name);
        }
        return document;
    }

    public Optional<CartTypeCatalogEntry> findCartType(String key) {
        return findEntry(CatalogName.CART_TYPES, CartTypeCatalogEntry.class, key);
    }

    public CartTypeCatalogEntry cartType(String key) {
        return findCartType(key).orElseThrow(() -> new IllegalArgumentException("Unknown cart type: " + key));
    }

    public Optional<UpgradeCatalogEntry> findUpgrade(String key) {
        return findEntry(CatalogName.UPGRADES, UpgradeCatalogEntry.class, key);
    }

    public UpgradeCatalogEntry upgrade(String key) {
        return findUpgrade(key).orElseThrow(() -> new IllegalArgumentException("Unknown upgrade: " + key));
    }

    public Optional<CargoCatalogEntry> findCargo(String key) {
        return findEntry(CatalogName.CARGO, CargoCatalogEntry.class, key);
    }

    public CargoCatalogEntry cargo(String key) {
        return findCargo(key).orElseThrow(() -> new IllegalArgumentException("Unknown cargo: " + key));
    }

    public Optional<RoleCatalogEntry> findRole(String key) {
        return findEntry(CatalogName.ROLES, RoleCatalogEntry.class, key);
    }

    public RoleCatalogEntry role(String key) {
        return findRole(key).orElseThrow(() -> new IllegalArgumentException("Unknown role: " + key));
    }

    public Optional<FeatCatalogEntry> findFeat(String key) {
        return findEntry(CatalogName.FEATS, FeatCatalogEntry.class, key);
    }

    public FeatCatalogEntry feat(String key) {
        return findFeat(key).orElseThrow(() -> new IllegalArgumentException("Unknown feat: " + key));
    }

    public Optional<BeastCatalogEntry> findBeast(String key) {
        return findEntry(CatalogName.BEASTS, BeastCatalogEntry.class, key);
    }

    public BeastCatalogEntry beast(String key) {
        return findBeast(key).orElseThrow(() -> new IllegalArgumentException("Unknown beast: " + key));
    }

    public List<CatalogSummary> summaries() {
        return documents.values().stream()
                .sorted((left, right) -> Integer.compare(left.catalogName().ordinal(), right.catalogName().ordinal()))
                .map(document -> new CatalogSummary(
                        document.catalogName(),
                        document.title(),
                        document.description(),
                        document.versionId(),
                        document.campaignAware(),
                        document.entries().size()))
                .toList();
    }

    private static CartTypeCatalogEntry cart(
            String key,
            String name,
            CartCategory category,
            String cost,
            Integer costCp,
            int hitPoints,
            int hardness,
            int propulsionRequirement,
            String towingCreatureLimit,
            int consumption,
            int passengerCapacity,
            int cargoCapacity,
            List<String> restrictions,
            List<String> effects,
            boolean campaignSpecific,
            String source) {
        return cart(key, name, category, cost, costCp == null ? null : BigDecimal.valueOf(costCp), hitPoints, hardness, propulsionRequirement, towingCreatureLimit, consumption, passengerCapacity, cargoCapacity, restrictions, effects, campaignSpecific, source, null);
    }

    private static CartTypeCatalogEntry cart(
            String key,
            String name,
            CartCategory category,
            String cost,
            BigDecimal costCp,
            int hitPoints,
            int hardness,
            int propulsionRequirement,
            String towingCreatureLimit,
            int consumption,
            int passengerCapacity,
            int cargoCapacity,
            List<String> restrictions,
            List<String> effects,
            boolean campaignSpecific,
            String source,
            String note) {
        return new CartTypeCatalogEntry(
                key,
                name,
                category,
                cost,
                costCp,
                hitPoints,
                hardness,
                propulsionRequirement,
                towingCreatureLimit,
                BigDecimal.valueOf(consumption),
                BigDecimal.valueOf(passengerCapacity),
                BigDecimal.valueOf(cargoCapacity),
                restrictions,
                effects,
                campaignSpecific,
                source,
                note);
    }

    private static UpgradeCatalogEntry upgrade(
            String key,
            String name,
            String cost,
            Integer costCp,
            String restriction,
            String stackingRule,
            List<String> incompatibilities,
            String effect,
            boolean campaignSpecific,
            String source,
            String note) {
        return new UpgradeCatalogEntry(
                key,
                name,
                cost,
                costCp == null ? null : BigDecimal.valueOf(costCp),
                restriction,
                stackingRule,
                incompatibilities,
                effect,
                campaignSpecific,
                source,
                note);
    }

    private static CargoCatalogEntry cargo(
            String key,
            String name,
            String cost,
            Object costCp,
            String capacity,
            String value,
            Object valueCp,
            String degradation,
            String detail,
            boolean campaignSpecific,
            String source,
            String note) {
        return new CargoCatalogEntry(
                key,
                name,
                cost,
                stringify(costCp),
                capacity,
                capacity,
                value,
                stringify(valueCp),
                degradation,
                detail,
                campaignSpecific,
                source,
                note);
    }

    private static RoleCatalogEntry role(
            String key,
            String name,
            String hardLimit,
            String requirement,
            String benefitSummary,
            boolean optionalSubsystem,
            boolean campaignSpecific,
            String source,
            String note) {
        return new RoleCatalogEntry(
                key,
                name,
                hardLimit,
                requirement,
                benefitSummary,
                optionalSubsystem,
                campaignSpecific,
                source,
                note);
    }

    private static FeatCatalogEntry feat(
            String key,
            String name,
            String requirement,
            String effect,
            String usageLimit,
            String stackingRule,
            String persistenceRule,
            boolean campaignSpecific,
            String source,
            String note) {
        return new FeatCatalogEntry(
                key,
                name,
                requirement,
                effect,
                usageLimit,
                stackingRule,
                persistenceRule,
                campaignSpecific,
                source,
                note);
    }

    private static BeastCatalogEntry beast(
            String key,
            String name,
            String priceBase,
            Integer priceBaseCp,
            String priceTrained,
            Integer priceTrainedCp,
            int strength,
            String size,
            int speedFeet,
            Integer temperatureAdaptation,
            String adaptationNotes,
            boolean campaignSpecific,
            String source,
            String note) {
        return new BeastCatalogEntry(
                key,
                name,
                priceBase,
                priceBaseCp == null ? null : BigDecimal.valueOf(priceBaseCp),
                priceTrained,
                priceTrainedCp == null ? null : BigDecimal.valueOf(priceTrainedCp),
                strength,
                size,
                speedFeet,
                temperatureAdaptation,
                adaptationNotes,
                campaignSpecific,
                source,
                note);
    }

    private static String source(String file, String section) {
        return file + "#" + section;
    }

    private static String stringify(Object value) {
        return value == null ? null : value.toString();
    }

    @SuppressWarnings("unchecked")
    private <T extends CatalogEntry> Optional<T> findEntry(CatalogName catalogName, Class<T> entryType, String key) {
        Objects.requireNonNull(catalogName, "catalogName must not be null");
        Objects.requireNonNull(entryType, "entryType must not be null");
        if (key == null || key.trim().isEmpty()) {
            return Optional.empty();
        }
        return catalog(catalogName).entries().stream()
                .filter(entryType::isInstance)
                .map(entryType::cast)
                .filter(entry -> entry.key().equalsIgnoreCase(key.trim()))
                .findFirst();
    }
}

