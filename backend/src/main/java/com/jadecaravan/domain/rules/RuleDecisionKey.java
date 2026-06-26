package com.jadecaravan.domain.rules;

public enum RuleDecisionKey {
    D_01_EXTENDED_SPACE_ROUNDING(
            "Redondeo de espacio extendido",
            "La actualización indica +25% de la capacidad base, mínimo 1 para viajeros y mínimo 2 para carga, pero no define el redondeo.",
            "ceil(capacidadBase × 25%), respetando el mínimo",
            null,
            RuleDecisionStatus.PENDING),
    D_02_TOWING_BONUS_STACKING(
            "Tiro de cuatro, seis y ocho caballos",
            "No queda completamente explícito si el bono de velocidad de los tres niveles se acumula cuando todos los carros poseen una cadena de mejoras hasta ocho.",
            "Acumular +4 por cada nivel universal instalado.",
            null,
            RuleDecisionStatus.PENDING),
    D_03_DISCONTENT_GAIN_ON_FAILURE_TABLE(
            "Ganancia de descontento por la tabla de sucesos",
            "La tabla proporciona dificultad y tres niveles de pérdida, pero no expresa con total claridad si el aumento de descontento es siempre 1 al fallar o coincide con gravedad 1/2/3.",
            "Aumento igual a la columna de pérdida fallida.",
            null,
            RuleDecisionStatus.PENDING),
    D_04_CONSECUTIVE_DISCONTENT_DAYS(
            "Días consecutivos de ganancia de descontento",
            "La tabla marca 3, 6 y 10 días, pero no explica el reinicio ni el comportamiento tras 10.",
            "El contador aumenta solo en días donde el descontento sube; se reinicia en cualquier día sin aumento.",
            null,
            RuleDecisionStatus.PENDING),
    D_05_STOVE_HEAT_SCOPE(
            "Estufa y ámbito de su calor",
            "La estufa duplica el beneficio del carbón para hasta 20 criaturas y no se acumula con carros aislados, pero no indica si calienta un conjunto de carros, un campamento o viajeros concretos.",
            "Tratarlo como una acción de campamento dirigida a viajeros, no a carros.",
            null,
            RuleDecisionStatus.PENDING),
    D_06_FRIDGE_PERISHABLE_FORMULA(
            "Efecto exacto de la nevera con hielo",
            "La regla dice que los perecederos pierden efectividad un día más tarde y que el hielo triplica la duración de sus efectos, pero no define una fórmula única de calendario.",
            "Sin hielo, degradación cada 3 días dentro de nevera; con hielo, hasta 10 unidades quedan protegidas durante 6 días.",
            null,
            RuleDecisionStatus.PENDING),
    D_07_COLD_INSULATION_PENALTY(
            "Aislamiento para el calor",
            "La actualización cambia explícitamente el aislamiento de frío a -4 millas/día una vez. No modifica el de calor.",
            "Aplicar -1 milla/día por carro con aislamiento de calor.",
            null,
            RuleDecisionStatus.PENDING),
    D_08_TAVERN_CART(
            "Carro taberna",
            "El Excel contiene un Carro taberna con estadísticas, pero no hay descripción ni beneficio en las reglas compartidas.",
            "Crear un CustomCartType solo para esta campaña, con las estadísticas observadas y sin beneficio hasta definirlo.",
            null,
            RuleDecisionStatus.PENDING),
    D_09_BASIC_TRADE_PROFIT(
            "Comercio básico",
            "La venta de una mercancía concede PO iguales al resultado de la prueba, pero no detalla si es precio total o beneficio neto.",
            "Tratarlo como ingreso total; el coste de compra ya quedó reflejado al adquirir el lote.",
            null,
            RuleDecisionStatus.PENDING),
    D_10_ROLE_LIMIT_AND_COMBINED_BONUSES(
            "Límites de roles y beneficios combinados",
            "Hay reglas de límite normal +5, Viajeros expertos, Héroes, trabajo en equipo, sirvientes y lanzadores de conjuros. El orden exacto no está declarado.",
            "Aplicar primero roles ordinarios, luego multiplicadores de equipo/sirviente, después el límite de roles y finalmente Héroe, dotes, carros, equipo y contexto fuera del límite.",
            null,
            RuleDecisionStatus.PENDING),
    D_11_EFFICIENT_CONSUMPTION_AND_IMPECCABLE_ORGANIZATION(
            "Consumo eficiente y Organización impecable",
            "Ambas dotes reducen consumo y pueden adquirirse varias veces.",
            "Aplicar reducciones aditivas, respetando el suelo indicado por Consumo eficiente.",
            null,
            RuleDecisionStatus.PENDING),
    D_12_SUCCESSIVE_CELEBRATIONS(
            "Celebraciones sucesivas",
            "El bono temporal de Moral no se acumula, pero la regla no indica si una nueva celebración sustituye una anterior menor.",
            "Conservar el mayor bono activo; si el nuevo es igual o mayor, renovar su duración a cinco días.",
            null,
            RuleDecisionStatus.PENDING),
    D_13_DESTRUCTION_RULE_PRIORITY(
            "Reglas oficiales de destrucción frente a las personalizadas",
            "La guía oficial presenta otro daño a tripulantes tras destrucción. El reglamento personalizado indica 2d10+5 a todos los tripulantes.",
            "Usar 2d10+5 por tener prioridad el reglamento personalizado.",
            "2d10+5",
            RuleDecisionStatus.RESOLVED),
    D_14_SLAVERY_SUBSYSTEM_ENABLED(
            "Autorización de reglas de esclavitud",
            "El reglamento incluye roles y dotes de esclavitud. Deben modelarse como subsistema opcional, apagado por defecto en nuevas campañas, sin eliminar los datos ni alterar la fidelidad de las reglas si se activa.",
            "Subsistema opcional, apagado por defecto en nuevas campañas.",
            null,
            RuleDecisionStatus.PENDING);

    private final String title;
    private final String description;
    private final String defaultProposal;
    private final String documentedChoice;
    private final RuleDecisionStatus initialStatus;

    RuleDecisionKey(
            String title,
            String description,
            String defaultProposal,
            String documentedChoice,
            RuleDecisionStatus initialStatus) {
        this.title = title;
        this.description = description;
        this.defaultProposal = defaultProposal;
        this.documentedChoice = documentedChoice;
        this.initialStatus = initialStatus;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public String defaultProposal() {
        return defaultProposal;
    }

    public String documentedChoice() {
        return documentedChoice;
    }

    public RuleDecisionStatus initialStatus() {
        return initialStatus;
    }

    public RuleDecision seedDecision() {
        return switch (initialStatus) {
            case PENDING -> RuleDecision.pending(this);
            case RESOLVED -> RuleDecision.resolved(
                    this,
                    documentedChoice,
                    null,
                    "Documented resolution in docs/10-decidir-antes-de-automatizar.md");
        };
    }
}
