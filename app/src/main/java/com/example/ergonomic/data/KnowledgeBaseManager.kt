package com.example.ergonomic.data

import android.content.Context
import com.example.ergonomic.model.*
import com.google.gson.Gson
import java.io.InputStreamReader

class KnowledgeBaseManager(private val context: Context) {

    private var knowledgeBase: KnowledgeBase? = null

    init {
        loadKnowledgeBase()
    }

    private fun loadKnowledgeBase() {
        try {
            val inputStream = context.assets.open("knowledge_base.json")
            val reader = InputStreamReader(inputStream)
            knowledgeBase = Gson().fromJson(reader, KnowledgeBase::class.java)
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ============================================================================
    // OBTENER RECOMENDACIONES POR PROBLEMA
    // ============================================================================

    fun getRecommendationByKey(key: String): DetailedRecommendation? {
        return knowledgeBase?.recommendations?.get(key)
    }

    fun getChairRecommendation(chairType: ChairType): DetailedRecommendation? {
        return when (chairType) {
            ChairType.INADEQUATE -> getRecommendationByKey("chair_inadequate")
            ChairType.BASIC -> getRecommendationByKey("chair_basic")
            else -> null
        }
    }

    fun getMonitorRecommendation(height: MonitorHeight): DetailedRecommendation? {
        return when (height) {
            MonitorHeight.ABOVE_EYES -> getRecommendationByKey("monitor_too_high")
            MonitorHeight.BELOW_15 -> getRecommendationByKey("monitor_too_low")
            else -> null
        }
    }

    fun getPauseRecommendation(frequency: PauseFrequency): DetailedRecommendation? {
        return when (frequency) {
            PauseFrequency.NEVER -> getRecommendationByKey("no_breaks")
            PauseFrequency.BATHROOM_ONLY -> getRecommendationByKey("insufficient_breaks")
            else -> null
        }
    }

    fun getLumbarRecommendation(hasSupport: Boolean, workHours: Int): DetailedRecommendation? {
        return if (!hasSupport && workHours >= 6) {
            getRecommendationByKey("no_lumbar_support")
        } else null
    }

    // ============================================================================
    // OBTENER EJERCICIOS
    // ============================================================================

    fun getExercisesByBodyPart(bodyPart: String): List<Exercise> {
        return knowledgeBase?.exercises?.get(bodyPart) ?: emptyList()
    }

    fun getAllExercises(): Map<String, List<Exercise>> {
        return knowledgeBase?.exercises ?: emptyMap()
    }

    fun getExercisesForSymptoms(symptoms: List<String>): List<Exercise> {
        val exercises = mutableListOf<Exercise>()

        symptoms.forEach { symptom ->
            when {
                symptom.contains("cervical", ignoreCase = true) ||
                        symptom.contains("cuello", ignoreCase = true) -> {
                    exercises.addAll(getExercisesByBodyPart("neck"))
                }
                symptom.contains("lumbar", ignoreCase = true) ||
                        symptom.contains("espalda", ignoreCase = true) -> {
                    exercises.addAll(getExercisesByBodyPart("back"))
                }
                symptom.contains("muñeca", ignoreCase = true) ||
                        symptom.contains("carpal", ignoreCase = true) -> {
                    exercises.addAll(getExercisesByBodyPart("wrists"))
                }
                symptom.contains("hombro", ignoreCase = true) -> {
                    exercises.addAll(getExercisesByBodyPart("shoulders"))
                }
            }
        }

        // Siempre incluir ejercicios de ojos
        exercises.addAll(getExercisesByBodyPart("eyes"))

        return exercises.distinctBy { it.name }
    }

    // ============================================================================
    // OBTENER PRODUCTOS RECOMENDADOS
    // ============================================================================

    fun getProductsByBudget(budget: String): List<Product> {
        return when (budget.lowercase()) {
            "bajo", "economico" -> knowledgeBase?.product_recommendations?.budget_friendly ?: emptyList()
            "medio" -> knowledgeBase?.product_recommendations?.medium_investment ?: emptyList()
            "alto", "premium" -> knowledgeBase?.product_recommendations?.premium ?: emptyList()
            else -> emptyList()
        }
    }

    fun getAllProducts(): ProductRecommendations? {
        return knowledgeBase?.product_recommendations
    }

    // ============================================================================
    // OBTENER DESCRIPCIONES DE RIESGO
    // ============================================================================

    fun getRiskDescription(riskLevel: RiskLevel): RiskDescription? {
        return knowledgeBase?.risk_descriptions?.get(riskLevel.name)
    }

    // ============================================================================
    // CONTENIDO EDUCATIVO
    // ============================================================================

    fun getProperPostureGuide(): ProperPosture? {
        return knowledgeBase?.educational_content?.proper_posture
    }

    fun getWarningSigns(): WarningSigns? {
        return knowledgeBase?.educational_content?.warning_signs
    }

    // ============================================================================
    // GENERAR RECOMENDACIONES COMPLETAS
    // ============================================================================

    fun generateEnhancedRecommendations(data: QuestionnaireData): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        // Silla
        data.chairType?.let { chairType ->
            getChairRecommendation(chairType)?.let { detail ->
                recommendations.add(
                    Recommendation(
                        priority = detail.priority,
                        category = detail.category,
                        title = detail.title,
                        description = detail.description,
                        actions = detail.actions
                    )
                )
            }
        }

        // Soporte lumbar
        if (data.lumbarSupport == LumbarSupport.NONE && data.workHours >= 6) {
            getLumbarRecommendation(false, data.workHours)?.let { detail ->
                recommendations.add(
                    Recommendation(
                        priority = detail.priority,
                        category = detail.category,
                        title = detail.title,
                        description = detail.description,
                        actions = detail.actions
                    )
                )
            }
        }

        // Monitor
        data.monitorHeight?.let { height ->
            getMonitorRecommendation(height)?.let { detail ->
                recommendations.add(
                    Recommendation(
                        priority = detail.priority,
                        category = detail.category,
                        title = detail.title,
                        description = detail.description,
                        actions = detail.actions
                    )
                )
            }
        }

        // Laptop sin soporte
        if (data.monitorType == "Solo laptop (sin soporte)") {
            getRecommendationByKey("laptop_without_support")?.let { detail ->
                recommendations.add(
                    Recommendation(
                        priority = detail.priority,
                        category = detail.category,
                        title = detail.title,
                        description = detail.description,
                        actions = detail.actions
                    )
                )
            }
        }

        // Pausas
        data.pauseFrequency?.let { frequency ->
            getPauseRecommendation(frequency)?.let { detail ->
                recommendations.add(
                    Recommendation(
                        priority = detail.priority,
                        category = detail.category,
                        title = detail.title,
                        description = detail.description,
                        actions = detail.actions
                    )
                )
            }
        }

        // Iluminación
        if (data.lighting == "Insuficiente/Tenue") {
            getRecommendationByKey("poor_lighting")?.let { detail ->
                recommendations.add(
                    Recommendation(
                        priority = detail.priority,
                        category = detail.category,
                        title = detail.title,
                        description = detail.description,
                        actions = detail.actions
                    )
                )
            }
        }

        // Reflejos
        if (data.screenGlare in listOf("Frecuentemente", "Constantemente")) {
            getRecommendationByKey("screen_glare")?.let { detail ->
                recommendations.add(
                    Recommendation(
                        priority = detail.priority,
                        category = detail.category,
                        title = detail.title,
                        description = detail.description,
                        actions = detail.actions
                    )
                )
            }
        }

        // Tiempo sentado continuo
        if (data.continuousSittingTime == "Más de 3 horas") {
            getRecommendationByKey("continuous_sitting")?.let { detail ->
                recommendations.add(
                    Recommendation(
                        priority = detail.priority,
                        category = detail.category,
                        title = detail.title,
                        description = detail.description,
                        actions = detail.actions
                    )
                )
            }
        }

        // Ordenar por prioridad
        return recommendations.sortedBy {
            when (it.priority) {
                "URGENTE" -> 0
                "CRITICO" -> 0
                "ALTA" -> 1
                "MEDIA" -> 2
                else -> 3
            }
        }
    }
    // Agregar estos métodos al final de KnowledgeBaseManager.kt

    /**
     * Obtener recomendaciones basadas en severidad de áreas críticas
     */
    fun getRecommendationsForSeverity(areaType: String, severity: Float): List<DetailedRecommendation> {
        val recommendations = mutableListOf<DetailedRecommendation>()

        when(areaType.lowercase()) {
            "silla", "chair" -> {
                if (severity > 0.7f) {
                    getRecommendationByKey("chair_inadequate")?.let { recommendations.add(it) }
                } else {
                    getRecommendationByKey("chair_basic")?.let { recommendations.add(it) }
                }
                getRecommendationByKey("no_lumbar_support")?.let { recommendations.add(it) }
            }
            "monitor" -> {
                getRecommendationByKey("monitor_too_high")?.let { recommendations.add(it) }
                getRecommendationByKey("monitor_too_low")?.let { recommendations.add(it) }
                getRecommendationByKey("laptop_without_support")?.let { recommendations.add(it) }
            }
            "pausas", "breaks" -> {
                getRecommendationByKey("no_breaks")?.let { recommendations.add(it) }
                getRecommendationByKey("insufficient_breaks")?.let { recommendations.add(it) }
            }
            "iluminación", "lighting" -> {
                getRecommendationByKey("poor_lighting")?.let { recommendations.add(it) }
                getRecommendationByKey("screen_glare")?.let { recommendations.add(it) }
            }
        }

        return recommendations
    }

    /**
     * Obtener quick wins (acciones rápidas de alto impacto)
     */
    fun getQuickWins(): List<QuickWin> {
        return listOf(
            QuickWin(
                area = "Monitor",
                title = "Ajustar altura de monitor",
                description = "Baja o sube el monitor para que el borde superior esté a nivel de ojos",
                timeMinutes = 5,
                impact = 0.9f,
                cost = "Gratis"
            ),
            QuickWin(
                area = "Pausas",
                title = "Configurar alarma de pausas",
                description = "Configura alarma cada 45 min en tu teléfono",
                timeMinutes = 2,
                impact = 0.85f,
                cost = "Gratis"
            ),
            QuickWin(
                area = "Silla",
                title = "Soporte lumbar temporal",
                description = "Usa toalla enrollada como soporte lumbar",
                timeMinutes = 3,
                impact = 0.7f,
                cost = "Gratis"
            ),
            QuickWin(
                area = "Iluminación",
                title = "Reposicionar monitor",
                description = "Coloca monitor perpendicular a ventanas para evitar reflejos",
                timeMinutes = 5,
                impact = 0.6f,
                cost = "Gratis"
            )
        )
    }

    /**
     * Obtener alivio inmediato para síntomas
     */
    fun getImmediateRelief(symptomType: String): ImmediateRelief? {
        return when(symptomType.lowercase()) {
            "cervical", "cuello", "neck" -> ImmediateRelief(
                symptom = "Dolor cervical",
                title = "Ejercicios de alivio cervical",
                description = "Rotaciones suaves de cuello (10 rep) + estiramiento lateral (15seg cada lado)",
                durationMinutes = 3,
                exercises = getExercisesByBodyPart("neck")
            )
            "lumbar", "espalda", "back" -> ImmediateRelief(
                symptom = "Dolor lumbar",
                title = "Estiramientos de espalda",
                description = "Inclinación pélvica sentado (15 rep) + rotación de tronco",
                durationMinutes = 4,
                exercises = getExercisesByBodyPart("back")
            )
            "muñecas", "wrist" -> ImmediateRelief(
                symptom = "Dolor de muñecas",
                title = "Estiramientos de muñeca",
                description = "Extensión y flexión de muñecas (15seg cada uno)",
                durationMinutes = 2,
                exercises = getExercisesByBodyPart("wrists")
            )
            "visual", "ojos", "eye" -> ImmediateRelief(
                symptom = "Fatiga visual",
                title = "Descanso visual",
                description = "Regla 20-20-20: Mira 20 pies lejos por 20 seg",
                durationMinutes = 1,
                exercises = getExercisesByBodyPart("eyes")
            )
            else -> null
        }
    }

    /**
     * Obtener explicación de impacto de un área
     */
    fun getAreaImpactExplanation(areaType: String, severity: Float): String {
        val severityText = when {
            severity > 0.8f -> "crítica"
            severity > 0.6f -> "alta"
            severity > 0.4f -> "moderada"
            else -> "baja"
        }

        return when(areaType.lowercase()) {
            "silla", "chair" ->
                "Configuración de silla $severityText. Una silla inadecuada causa el ${(severity * 100).toInt()}% de los problemas lumbares."
            "monitor" ->
                "Posición de monitor $severityText. Altura incorrecta causa tensión cervical en el ${(severity * 100).toInt()}% de casos."
            "pausas", "breaks" ->
                "Frecuencia de pausas $severityText. Sin pausas adecuadas, el riesgo de TME aumenta ${(severity * 100).toInt()}%."
            "iluminación", "lighting" ->
                "Condiciones de iluminación ${severityText}. Mala iluminación causa fatiga visual en el ${(severity * 100).toInt()}% de usuarios."
            else ->
                "Área con severidad $severityText (${(severity * 100).toInt()}%)"
        }
    }

    // Data classes para las nuevas funcionalidades
    data class QuickWin(
        val area: String,
        val title: String,
        val description: String,
        val timeMinutes: Int,
        val impact: Float,
        val cost: String
    )

    data class ImmediateRelief(
        val symptom: String,
        val title: String,
        val description: String,
        val durationMinutes: Int,
        val exercises: List<Exercise>
    )
}
