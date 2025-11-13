package com.example.ergonomic.engine

import com.example.ergonomic.data.KnowledgeBase
import com.example.ergonomic.ml.MLPrediction

/**
 * NIVEL 3: MOTOR DE GENERACIÓN
 * Combina predicciones ML + Knowledge Base → Reporte Personalizado
 */
class ReportGenerator(
    private val knowledgeBase: KnowledgeBase
) {

    /**
     * Genera reporte personalizado completo
     */
    fun generateReport(
        mlPrediction: MLPrediction,
        userContext: UserContext
    ): PersonalizedReport {

        // 1. Obtener recomendaciones basadas en áreas críticas detectadas por ML
        val recommendations = getRecommendationsForAreas(
            criticalAreas = mlPrediction.criticalAreas,
            confidence = mlPrediction.confidence
        )

        // 2. Priorizar según severidad ML
        val prioritized = prioritizeRecommendations(
            recommendations = recommendations,
            mlPrediction = mlPrediction
        )

        // 3. Generar acciones inmediatas
        val immediateActions = generateImmediateActions(
            mlPrediction = mlPrediction,
            userContext = userContext
        )

        // 4. Generar plan de mejora
        val improvementPlan = generateImprovementPlan(
            prioritized = prioritized,
            timeframe = 30 // días
        )

        // 5. Generar explicación contextual
        val explanation = generateContextualExplanation(
            mlPrediction = mlPrediction,
            userContext = userContext
        )

        return PersonalizedReport(
            riskLevel = mlPrediction.riskLevel,
            confidence = mlPrediction.confidence,
            explanation = explanation,
            criticalAreas = mlPrediction.criticalAreas,
            likelySymptoms = mlPrediction.likelySymptoms,
            immediateActions = immediateActions,
            recommendations = prioritized,
            improvementPlan = improvementPlan,
            estimatedImpact = calculateEstimatedImpact(prioritized)
        )
    }

    /**
     * Obtiene recomendaciones específicas para áreas críticas
     */
    private fun getRecommendationsForAreas(
        criticalAreas: List<CriticalArea>,
        confidence: Float
    ): List<Recommendation> {
        return criticalAreas.flatMap { area ->
            val areaRecommendations = when(area.type) {
                AreaType.CHAIR -> knowledgeBase.getChairRecommendations(area.severity)
                AreaType.MONITOR -> knowledgeBase.getMonitorRecommendations(area.severity)
                AreaType.BREAKS -> knowledgeBase.getBreakRecommendations(area.severity)
                AreaType.LIGHTING -> knowledgeBase.getLightingRecommendations(area.severity)
                AreaType.POSTURE -> knowledgeBase.getPostureRecommendations(area.severity)
                AreaType.KEYBOARD -> knowledgeBase.getKeyboardRecommendations(area.severity)
                AreaType.MOUSE -> knowledgeBase.getMouseRecommendations(area.severity)
                AreaType.DESK -> knowledgeBase.getDeskRecommendations(area.severity)
            }

            // Filtrar por nivel de confianza
            areaRecommendations.filter { rec ->
                confidence >= rec.minimumConfidenceRequired
            }
        }
    }

    /**
     * Prioriza recomendaciones según severidad ML y facilidad de implementación
     */
    private fun prioritizeRecommendations(
        recommendations: List<Recommendation>,
        mlPrediction: MLPrediction
    ): List<PrioritizedRecommendation> {
        return recommendations.map { rec ->
            // Calcular score de prioridad
            val severityScore = calculateSeverityScore(rec, mlPrediction)
            val impactScore = rec.estimatedImpact
            val effortScore = 1.0f - rec.implementationDifficulty

            // Fórmula de priorización: (Impacto * Severidad) / Esfuerzo
            val priorityScore = (impactScore * severityScore) / (rec.implementationDifficulty + 0.1f)

            PrioritizedRecommendation(
                recommendation = rec,
                priorityScore = priorityScore,
                urgency = calculateUrgency(severityScore, mlPrediction.riskLevel)
            )
        }.sortedByDescending { it.priorityScore }
    }

    /**
     * Genera acciones inmediatas (quick wins)
     */
    private fun generateImmediateActions(
        mlPrediction: MLPrediction,
        userContext: UserContext
    ): List<ImmediateAction> {
        val actions = mutableListOf<ImmediateAction>()

        // Acciones basadas en riesgo crítico
        if (mlPrediction.riskLevel == RiskLevel.CRITICAL) {
            actions.add(ImmediateAction(
                title = "🚨 Acción Urgente",
                description = "Tu nivel de riesgo es CRÍTICO. Toma un descanso de 15 minutos ahora mismo.",
                timeRequired = 15,
                impact = Impact.HIGH,
                category = ActionCategory.BREAK
            ))
        }

        // Acciones basadas en síntomas probables
        mlPrediction.likelySymptoms.filter { it.probability > 0.7f }.forEach { symptom ->
            knowledgeBase.getImmediateReliefFor(symptom.type)?.let { relief ->
                actions.add(ImmediateAction(
                    title = "💊 Alivio para ${symptom.name}",
                    description = relief.description,
                    timeRequired = relief.durationMinutes,
                    impact = Impact.MEDIUM,
                    category = ActionCategory.RELIEF
                ))
            }
        }

        // Acciones quick-win (bajo esfuerzo, alto impacto)
        mlPrediction.criticalAreas.forEach { area ->
            knowledgeBase.getQuickWinsFor(area.type)?.let { quickWin ->
                actions.add(ImmediateAction(
                    title = "⚡ Quick Win: ${area.name}",
                    description = quickWin.description,
                    timeRequired = quickWin.durationMinutes,
                    impact = Impact.HIGH,
                    category = ActionCategory.QUICK_WIN
                ))
            }
        }

        return actions.sortedByDescending {
            it.impact.value / (it.timeRequired + 1)
        }.take(3)
    }

    /**
     * Genera plan de mejora escalonado (30 días)
     */
    private fun generateImprovementPlan(
        prioritized: List<PrioritizedRecommendation>,
        timeframe: Int
    ): ImprovementPlan {
        val weeks = mutableListOf<WeekPlan>()
        val itemsPerWeek = (prioritized.size / 4.0).toInt().coerceAtLeast(1)

        prioritized.chunked(itemsPerWeek).forEachIndexed { index, weekRecommendations ->
            if (index < 4) { // 4 semanas
                weeks.add(WeekPlan(
                    weekNumber = index + 1,
                    focus = determineWeekFocus(weekRecommendations),
                    recommendations = weekRecommendations,
                    estimatedImpact = weekRecommendations.sumOf {
                        it.recommendation.estimatedImpact.toDouble()
                    }.toFloat()
                ))
            }
        }

        return ImprovementPlan(
            totalDays = timeframe,
            weeks = weeks,
            expectedRiskReduction = calculateExpectedReduction(prioritized),
            milestones = generateMilestones(weeks)
        )
    }

    /**
     * Genera explicación contextual del análisis
     */
    private fun generateContextualExplanation(
        mlPrediction: MLPrediction,
        userContext: UserContext
    ): String {
        val sb = StringBuilder()

        // Introducción basada en nivel de riesgo
        sb.append(getRiskLevelIntro(mlPrediction.riskLevel, mlPrediction.confidence))
        sb.append("\n\n")

        // Explicar áreas críticas
        sb.append("🎯 **Áreas que requieren atención:**\n\n")
        mlPrediction.criticalAreas.take(3).forEach { area ->
            sb.append("• **${area.name}**: ")
            sb.append(explainAreaImpact(area, userContext))
            sb.append("\n")
        }
        sb.append("\n")

        // Explicar síntomas probables
        val highProbSymptoms = mlPrediction.likelySymptoms.filter { it.probability > 0.6f }
        if (highProbSymptoms.isNotEmpty()) {
            sb.append("⚠️ **Síntomas que podrías desarrollar:**\n\n")
            highProbSymptoms.take(3).forEach { symptom ->
                sb.append("• ${symptom.name} (${(symptom.probability * 100).toInt()}% probabilidad)\n")
            }
            sb.append("\n")
        }

        // Mensaje motivacional
        sb.append(getMotivationalMessage(mlPrediction.riskLevel))

        return sb.toString()
    }

    // ============ HELPER FUNCTIONS ============

    private fun calculateSeverityScore(
        rec: Recommendation,
        mlPrediction: MLPrediction
    ): Float {
        val area = mlPrediction.criticalAreas.find { it.type == rec.targetArea }
        return area?.severity ?: 0.5f
    }

    private fun calculateUrgency(severityScore: Float, riskLevel: RiskLevel): Urgency {
        return when {
            riskLevel == RiskLevel.CRITICAL -> Urgency.IMMEDIATE
            severityScore > 0.8f -> Urgency.HIGH
            severityScore > 0.6f -> Urgency.MEDIUM
            else -> Urgency.LOW
        }
    }

    private fun determineWeekFocus(recommendations: List<PrioritizedRecommendation>): String {
        val mainArea = recommendations.firstOrNull()?.recommendation?.targetArea
        return when(mainArea) {
            AreaType.CHAIR -> "Optimización de asiento"
            AreaType.MONITOR -> "Configuración de pantalla"
            AreaType.BREAKS -> "Establecer rutina de pausas"
            AreaType.POSTURE -> "Corrección postural"
            else -> "Mejoras generales"
        }
    }

    private fun calculateExpectedReduction(
        prioritized: List<PrioritizedRecommendation>
    ): Float {
        // Estimación optimista: suma de impactos ponderados
        val totalImpact = prioritized.take(10).sumOf {
            it.recommendation.estimatedImpact.toDouble()
        }
        return (totalImpact * 0.7f).coerceIn(0.3f, 0.9f).toFloat()
    }

    private fun generateMilestones(weeks: List<WeekPlan>): List<Milestone> {
        return listOf(
            Milestone(1, "Primeras mejoras", "Configuración básica completa"),
            Milestone(2, "Hábitos establecidos", "Pausas y ejercicios regulares"),
            Milestone(3, "Optimización", "Ajustes finos y productos adecuados"),
            Milestone(4, "Evaluación", "Medir progreso y ajustar plan")
        )
    }

    private fun calculateEstimatedImpact(
        prioritized: List<PrioritizedRecommendation>
    ): EstimatedImpact {
        val shortTerm = prioritized.take(3).sumOf {
            it.recommendation.estimatedImpact.toDouble()
        }.toFloat() / 3f

        val longTerm = prioritized.take(10).sumOf {
            it.recommendation.estimatedImpact.toDouble()
        }.toFloat() / 10f

        return EstimatedImpact(
            shortTerm = shortTerm, // 1 semana
            mediumTerm = (shortTerm + longTerm) / 2f, // 2 semanas
            longTerm = longTerm // 4 semanas
        )
    }

    private fun getRiskLevelIntro(level: RiskLevel, confidence: Float): String {
        val confidenceText = when {
            confidence > 0.9f -> "con alta certeza"
            confidence > 0.7f -> "con buena confianza"
            else -> "según el análisis"
        }

        return when(level) {
            RiskLevel.LOW -> "✅ Tu configuración ergonómica es buena, $confidenceText. Hay algunas áreas de mejora menor."
            RiskLevel.MEDIUM -> "⚠️ Tu configuración tiene riesgo moderado, $confidenceText. Es importante hacer ajustes preventivos."
            RiskLevel.HIGH -> "🔴 Tu configuración presenta riesgo alto, $confidenceText. Se requieren cambios urgentes."
            RiskLevel.CRITICAL -> "🚨 Tu configuración es crítica, $confidenceText. Actúa inmediatamente para prevenir lesiones."
        }
    }

    private fun explainAreaImpact(area: CriticalArea, context: UserContext): String {
        return knowledgeBase.getImpactExplanation(area.type, area.severity) ?:
        "Requiere atención (severidad: ${(area.severity * 100).toInt()}%)"
    }

    private fun getMotivationalMessage(level: RiskLevel): String {
        return when(level) {
            RiskLevel.LOW -> "💪 ¡Buen trabajo! Pequeños ajustes te llevarán a la configuración perfecta."
            RiskLevel.MEDIUM -> "🎯 Siguiendo este plan, puedes reducir tu riesgo significativamente en 2-3 semanas."
            RiskLevel.HIGH -> "⚡ Los cambios que hagas ahora prevendrán problemas serios. ¡Empieza hoy!"
            RiskLevel.CRITICAL -> "🚨 Tu salud es prioridad. Cada acción que tomes tendrá un impacto inmediato positivo."
        }
    }
}

// ============ DATA CLASSES ============

data class PersonalizedReport(
    val riskLevel: RiskLevel,
    val confidence: Float,
    val explanation: String,
    val criticalAreas: List<CriticalArea>,
    val likelySymptoms: List<LikelySymptom>,
    val immediateActions: List<ImmediateAction>,
    val recommendations: List<PrioritizedRecommendation>,
    val improvementPlan: ImprovementPlan,
    val estimatedImpact: EstimatedImpact
)

data class UserContext(
    val workHours: Int,
    val hasExistingPain: Boolean,
    val budget: Budget,
    val age: Int?,
    val previousIssues: List<String>
)

data class PrioritizedRecommendation(
    val recommendation: Recommendation,
    val priorityScore: Float,
    val urgency: Urgency
)

data class Recommendation(
    val title: String,
    val description: String,
    val targetArea: AreaType,
    val estimatedImpact: Float, // 0.0 - 1.0
    val implementationDifficulty: Float, // 0.0 - 1.0
    val cost: Cost,
    val timeToImplement: Int, // minutos
    val minimumConfidenceRequired: Float = 0.5f
)

data class ImmediateAction(
    val title: String,
    val description: String,
    val timeRequired: Int, // minutos
    val impact: Impact,
    val category: ActionCategory
)

data class ImprovementPlan(
    val totalDays: Int,
    val weeks: List<WeekPlan>,
    val expectedRiskReduction: Float,
    val milestones: List<Milestone>
)

data class WeekPlan(
    val weekNumber: Int,
    val focus: String,
    val recommendations: List<PrioritizedRecommendation>,
    val estimatedImpact: Float
)

data class Milestone(
    val weekNumber: Int,
    val title: String,
    val description: String
)

data class EstimatedImpact(
    val shortTerm: Float,  // 1 semana
    val mediumTerm: Float, // 2 semanas
    val longTerm: Float    // 4 semanas
)

enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }
enum class AreaType { CHAIR, MONITOR, BREAKS, LIGHTING, POSTURE, KEYBOARD, MOUSE, DESK }
enum class Urgency { LOW, MEDIUM, HIGH, IMMEDIATE }
enum class Impact(val value: Float) { LOW(0.3f), MEDIUM(0.6f), HIGH(0.9f) }
enum class ActionCategory { BREAK, RELIEF, QUICK_WIN, ADJUSTMENT }
enum class Budget { LOW, MEDIUM, HIGH }
enum class Cost { FREE, LOW, MEDIUM, HIGH }