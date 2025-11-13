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
}