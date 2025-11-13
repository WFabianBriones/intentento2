package com.example.ergonomic.ml

import com.example.ergonomic.model.RiskLevel

/**
 * Extensión de MLPrediction para incluir datos del Motor de Generación
 */
data class EnhancedMLPrediction(
    val riskLevel: RiskLevel,
    val riskProbabilities: RiskProbabilities,
    val confidence: Float,
    val criticalAreas: List<CriticalAreaDetail>,
    val likelySymptoms: List<LikelySymptomDetail>,
    val riskScore: Int
)

data class RiskProbabilities(
    val low: Float,
    val medium: Float,
    val high: Float,
    val critical: Float
) {
    companion object {
        fun fromArray(probs: FloatArray): RiskProbabilities {
            require(probs.size == 4) { "Se esperan 4 probabilidades" }
            return RiskProbabilities(
                low = probs[0],
                medium = probs[1],
                high = probs[2],
                critical = probs[3]
            )
        }
    }
}

data class CriticalAreaDetail(
    val type: AreaType,
    val name: String,
    val severity: Float,
    val description: String,
    val isUrgent: Boolean = severity > 0.8f
)

data class LikelySymptomDetail(
    val type: SymptomType,
    val name: String,
    val probability: Float,
    val severity: SymptomSeverity,
    val relatedAreas: List<AreaType>
)

enum class AreaType(val displayName: String, val icon: String) {
    CHAIR("Silla", "🪑"),
    MONITOR("Monitor", "🖥️"),
    BREAKS("Pausas", "⏰"),
    LIGHTING("Iluminación", "💡"),
    POSTURE("Postura", "🧍"),
    KEYBOARD("Teclado", "⌨️"),
    MOUSE("Mouse", "🖱️"),
    DESK("Escritorio", "🗄️");

    companion object {
        fun fromString(name: String): AreaType? {
            return when (name) {
                "Silla" -> CHAIR
                "Monitor" -> MONITOR
                "Pausas" -> BREAKS
                "Iluminación" -> LIGHTING
                "Postura" -> POSTURE
                "Teclado" -> KEYBOARD
                "Mouse" -> MOUSE
                "Escritorio" -> DESK
                else -> null
            }
        }
    }
}

enum class SymptomType(
    val displayName: String,
    val relatedAreas: List<AreaType>
) {
    NECK_PAIN("Dolor cervical", listOf(AreaType.MONITOR, AreaType.CHAIR)),
    LOWER_BACK_PAIN("Dolor lumbar", listOf(AreaType.CHAIR, AreaType.POSTURE)),
    WRIST_PAIN("Dolor de muñecas", listOf(AreaType.KEYBOARD, AreaType.MOUSE)),
    EYE_STRAIN("Fatiga visual", listOf(AreaType.MONITOR, AreaType.LIGHTING));

    companion object {
        fun fromString(name: String): SymptomType? {
            return when {
                name.contains("cervical", ignoreCase = true) -> NECK_PAIN
                name.contains("lumbar", ignoreCase = true) -> LOWER_BACK_PAIN
                name.contains("muñecas", ignoreCase = true) -> WRIST_PAIN
                name.contains("visual", ignoreCase = true) -> EYE_STRAIN
                else -> null
            }
        }
    }
}

enum class SymptomSeverity {
    MINIMAL, MILD, MODERATE, SEVERE
}

/**
 * Convierte MLPrediction actual a EnhancedMLPrediction
 */
fun MLPrediction.toEnhanced(): EnhancedMLPrediction {
    // Calcular probabilidades desde riskLevel
    val probs = when(this.riskLevel) {
        RiskLevel.BAJO -> RiskProbabilities(0.7f, 0.2f, 0.08f, 0.02f)
        RiskLevel.MEDIO -> RiskProbabilities(0.2f, 0.6f, 0.15f, 0.05f)
        RiskLevel.ALTO -> RiskProbabilities(0.05f, 0.15f, 0.65f, 0.15f)
        RiskLevel.CRITICO -> RiskProbabilities(0.02f, 0.08f, 0.25f, 0.65f)
    }

    // Convertir áreas críticas
    val areas = this.criticalAreas.map { areaName ->
        val areaType = AreaType.fromString(areaName) ?: AreaType.CHAIR
        CriticalAreaDetail(
            type = areaType,
            name = areaName,
            severity = 0.75f, // Valor por defecto
            description = "Área identificada como crítica",
            isUrgent = this.riskLevel in listOf(RiskLevel.ALTO, RiskLevel.CRITICO)
        )
    }

    // Convertir síntomas
    val symptoms = this.symptomsProbabilities.map { (symptomName, prob) ->
        val symptomType = SymptomType.fromString(symptomName) ?: SymptomType.NECK_PAIN
        LikelySymptomDetail(
            type = symptomType,
            name = symptomName,
            probability = prob,
            severity = when {
                prob > 0.8f -> SymptomSeverity.SEVERE
                prob > 0.6f -> SymptomSeverity.MODERATE
                prob > 0.4f -> SymptomSeverity.MILD
                else -> SymptomSeverity.MINIMAL
            },
            relatedAreas = symptomType.relatedAreas
        )
    }

    return EnhancedMLPrediction(
        riskLevel = this.riskLevel,
        riskProbabilities = probs,
        confidence = this.confidence,
        criticalAreas = areas,
        likelySymptoms = symptoms,
        riskScore = this.riskScore
    )
}