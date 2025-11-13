package com.example.ergonomic.engine

import com.example.ergonomic.data.KnowledgeBaseManager
import com.example.ergonomic.ml.EnhancedMLPrediction
import com.example.ergonomic.ml.AreaType
import com.example.ergonomic.ml.LikelySymptomDetail
import com.example.ergonomic.ml.CriticalAreaDetail
import com.example.ergonomic.model.RiskLevel

/**
 * NIVEL 3: MOTOR DE GENERACIÓN
 * Combina predicciones ML + Knowledge Base → Reporte Personalizado
 */
class ReportGenerator(
    private val knowledgeBase: KnowledgeBaseManager
) {

    fun generateReport(
        mlPrediction: EnhancedMLPrediction,
        userContext: UserContext
    ): PersonalizedReport {

        val recommendations = getRecommendationsForAreas(
            criticalAreas = mlPrediction.criticalAreas,
            confidence = mlPrediction.confidence
        )

        val prioritized = prioritizeRecommendations(
            recommendations = recommendations,
            mlPrediction = mlPrediction
        )

        val immediateActions = generateImmediateActions(
            mlPrediction = mlPrediction,
            userContext = userContext
        )

        val improvementPlan = generateImprovementPlan(
            prioritized = prioritized,
            timeframe = 30
        )

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

    private fun getRecommendationsForAreas(
        criticalAreas: List<CriticalAreaDetail>,
        confidence: Float
    ): List<Recommendation> {
        return criticalAreas.flatMap { area ->
            val detailedRecs = knowledgeBase.getRecommendationsForSeverity(
                area.type.displayName,
                area.severity
            )

            detailedRecs.map { detail ->
                Recommendation(
                    title = detail.title,
                    description = detail.description,
                    targetArea = area.type,
                    estimatedImpact = when(detail.priority) {
                        "URGENTE", "CRITICO" -> 0.9f
                        "ALTA" -> 0.75f
                        "MEDIA" -> 0.5f
                        else -> 0.3f
                    },
                    implementationDifficulty = 0.3f,
                    cost = Cost.FREE,
                    timeToImplement = 15,
                    minimumConfidenceRequired = 0.5f
                )
            }
        }
    }

    private fun prioritizeRecommendations(
        recommendations: List<Recommendation>,
        mlPrediction: EnhancedMLPrediction
    ): List<PrioritizedRecommendation> {
        return recommendations.map { rec ->
            val severityScore = calculateSeverityScore(rec, mlPrediction)
            val impactScore = rec.estimatedImpact
            val priorityScore = (impactScore * severityScore) / (rec.implementationDifficulty + 0.1f)

            PrioritizedRecommendation(
                recommendation = rec,
                priorityScore = priorityScore,
                urgency = calculateUrgency(severityScore, mlPrediction.riskLevel)
            )
        }.sortedByDescending { it.priorityScore }
    }

    private fun generateImmediateActions(
        mlPrediction: EnhancedMLPrediction,
        userContext: UserContext
    ): List<ImmediateAction> {
        val actions = mutableListOf<ImmediateAction>()

        if (mlPrediction.riskLevel == RiskLevel.CRITICO) {
            actions.add(ImmediateAction(
                title = "🚨 Acción Urgente",
                description = "Tu nivel de riesgo es CRÍTICO. Toma un descanso de 15 minutos ahora mismo.",
                timeRequired = 15,
                impact = Impact.HIGH,
                category = ActionCategory.BREAK
            ))
        }

        mlPrediction.likelySymptoms.filter { it.probability > 0.7f }.forEach { symptom ->
            knowledgeBase.getImmediateRelief(symptom.type.displayName)?.let { relief ->
                actions.add(ImmediateAction(
                    title = "💊 Alivio para ${symptom.name}",
                    description = relief.description,
                    timeRequired = relief.durationMinutes,
                    impact = Impact.MEDIUM,
                    category = ActionCategory.RELIEF
                ))
            }
        }

        knowledgeBase.getQuickWins().take(2).forEach { quickWin ->
            actions.add(ImmediateAction(
                title = "⚡ Quick Win: ${quickWin.area}",
                description = quickWin.description,
                timeRequired = quickWin.timeMinutes,
                impact = Impact.HIGH,
                category = ActionCategory.QUICK_WIN
            ))
        }

        return actions.sortedByDescending {
            it.impact.value / (it.timeRequired + 1)
        }.take(3)
    }

    private fun generateImprovementPlan(
        prioritized: List<PrioritizedRecommendation>,
        timeframe: Int
    ): ImprovementPlan {
        val weeks = mutableListOf<WeekPlan>()
        val itemsPerWeek = (prioritized.size / 4.0).toInt().coerceAtLeast(1)

        prioritized.chunked(itemsPerWeek).forEachIndexed { index, weekRecommendations ->
            if (index < 4) {
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

    private fun generateContextualExplanation(
        mlPrediction: EnhancedMLPrediction,
        userContext: UserContext
    ): String {
        val sb = StringBuilder()

        sb.append(getRiskLevelIntro(mlPrediction.riskLevel, mlPrediction.confidence))
        sb.append("\n\n")

        sb.append("🎯 **Áreas que requieren atención:**\n\n")
        mlPrediction.criticalAreas.take(3).forEach { area ->
            sb.append("• **${area.name}**: ")
            sb.append(knowledgeBase.getAreaImpactExplanation(area.type.displayName, area.severity))
            sb.append("\n")
        }
        sb.append("\n")

        val highProbSymptoms = mlPrediction.likelySymptoms.filter { it.probability > 0.6f }
        if (highProbSymptoms.isNotEmpty()) {
            sb.append("⚠️ **Síntomas que podrías desarrollar:**\n\n")
            highProbSymptoms.take(3).forEach { symptom ->
                sb.append("• ${symptom.name} (${(symptom.probability * 100).toInt()}% probabilidad)\n")
            }
            sb.append("\n")
        }

        sb.append(getMotivationalMessage(mlPrediction.riskLevel))

        return sb.toString()
    }

    // Helper functions
    private fun calculateSeverityScore(rec: Recommendation, mlPrediction: EnhancedMLPrediction): Float {
        val area = mlPrediction.criticalAreas.find { it.type == rec.targetArea }
        return area?.severity ?: 0.5f
    }

    private fun calculateUrgency(severityScore: Float, riskLevel: RiskLevel): Urgency {
        return when {
            riskLevel == RiskLevel.CRITICO -> Urgency.IMMEDIATE
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

    private fun calculateExpectedReduction(prioritized: List<PrioritizedRecommendation>): Float {
        val totalImpact = prioritized.take(10).sumOf {
            it.recommendation.estimatedImpact.toDouble()
        }
        // CORRECCIÓN: Usar Double literals (0.7, 0.3, 0.9) ya que totalImpact es Double
        return (totalImpact * 0.7).coerceIn(0.3, 0.9).toFloat()
    }

    private fun generateMilestones(weeks: List<WeekPlan>): List<Milestone> {
        return listOf(
            Milestone(1, "Primeras mejoras", "Configuración básica completa"),
            Milestone(2, "Hábitos establecidos", "Pausas y ejercicios regulares"),
            Milestone(3, "Optimización", "Ajustes finos y productos adecuados"),
            Milestone(4, "Evaluación", "Medir progreso y ajustar plan")
        )
    }

    private fun calculateEstimatedImpact(prioritized: List<PrioritizedRecommendation>): EstimatedImpact {
        val shortTerm = prioritized.take(3).sumOf {
            it.recommendation.estimatedImpact.toDouble()
        }.toFloat() / 3f

        val longTerm = prioritized.take(10).sumOf {
            it.recommendation.estimatedImpact.toDouble()
        }.toFloat() / 10f

        return EstimatedImpact(
            shortTerm = shortTerm,
            mediumTerm = (shortTerm + longTerm) / 2f,
            longTerm = longTerm
        )
    }

    private fun getRiskLevelIntro(level: RiskLevel, confidence: Float): String {
        val confidenceText = when {
            confidence > 0.9f -> "con alta certeza"
            confidence > 0.7f -> "con buena confianza"
            else -> "según el análisis"
        }

        return when(level) {
            RiskLevel.BAJO -> "✅ Tu configuración ergonómica es buena, $confidenceText. Hay algunas áreas de mejora menor."
            RiskLevel.MEDIO -> "⚠️ Tu configuración tiene riesgo moderado, $confidenceText. Es importante hacer ajustes preventivos."
            RiskLevel.ALTO -> "🔴 Tu configuración presenta riesgo alto, $confidenceText. Se requieren cambios urgentes."
            RiskLevel.CRITICO -> "🚨 Tu configuración es crítica, $confidenceText. Actúa inmediatamente para prevenir lesiones."
        }
    }

    private fun getMotivationalMessage(level: RiskLevel): String {
        return when(level) {
            RiskLevel.BAJO -> "💪 ¡Buen trabajo! Pequeños ajustes te llevarán a la configuración perfecta."
            RiskLevel.MEDIO -> "🎯 Siguiendo este plan, puedes reducir tu riesgo significativamente en 2-3 semanas."
            RiskLevel.ALTO -> "⚡ Los cambios que hagas ahora prevendrán problemas serios. ¡Empieza hoy!"
            RiskLevel.CRITICO -> "🚨 Tu salud es prioridad. Cada acción que tomes tendrá un impacto inmediato positivo."
        }
    }
}

// Data classes
data class PersonalizedReport(
    val riskLevel: RiskLevel,
    val confidence: Float,
    val explanation: String,
    val criticalAreas: List<CriticalAreaDetail>,
    val likelySymptoms: List<LikelySymptomDetail>,
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
    val estimatedImpact: Float,
    val implementationDifficulty: Float,
    val cost: Cost,
    val timeToImplement: Int,
    val minimumConfidenceRequired: Float = 0.5f
)

data class ImmediateAction(
    val title: String,
    val description: String,
    val timeRequired: Int,
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
    val shortTerm: Float,
    val mediumTerm: Float,
    val longTerm: Float
)

enum class Urgency { LOW, MEDIUM, HIGH, IMMEDIATE }
enum class Impact(val value: Float) { LOW(0.3f), MEDIUM(0.6f), HIGH(0.9f) }
enum class ActionCategory { BREAK, RELIEF, QUICK_WIN, ADJUSTMENT }
enum class Budget { LOW, MEDIUM, HIGH }
enum class Cost { FREE, LOW, MEDIUM, HIGH }