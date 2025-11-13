package com.example.ergonomic.model

enum class RiskLevel {
    BAJO, MEDIO, ALTO, CRITICO
}

enum class ChairType(val value: String) {
    ERGONOMIC_FULL("Silla ergonómica ajustable (altura, respaldo, apoyabrazos)"),
    ERGONOMIC_BASIC("Silla con ajuste de altura y respaldo"),
    HEIGHT_ONLY("Silla con ajuste de altura solamente"),
    BASIC("Silla básica sin ajustes"),
    INADEQUATE("Silla inadecuada (comedor, cocina, etc.)")
}

enum class LumbarSupport(val value: String) {
    ADJUSTABLE("Sí, ajustable"),
    FIXED("Sí, fijo"),
    NONE("No tiene")
}

enum class MonitorHeight(val value: String) {
    EYE_LEVEL("A la altura de los ojos (correcto)"),
    BELOW_10_15("10-15cm por debajo de los ojos (correcto)"),
    ABOVE_EYES("Por encima de los ojos"),
    BELOW_15("Más de 15cm por debajo de los ojos"),
    VARIES("Varía constantemente")
}

enum class PauseFrequency(val value: String) {
    EVERY_30_60("Sí, cada 30-60 minutos"),
    EVERY_1_2_HOURS("Sí, cada 1-2 horas"),
    EVERY_3_4_HOURS("Sí, cada 3-4 horas"),
    BATHROOM_ONLY("Solo cuando voy al baño"),
    NEVER("Nunca/Muy rara vez")
}