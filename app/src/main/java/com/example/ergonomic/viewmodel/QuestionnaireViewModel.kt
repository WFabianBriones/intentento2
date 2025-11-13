package com.example.ergonomic.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ergonomic.data.KnowledgeBaseManager
import com.example.ergonomic.ml.ErgonomicMLModel
import com.example.ergonomic.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestionnaireViewModel : ViewModel() {

    private val _questionnaireData = MutableStateFlow(QuestionnaireData())
    val questionnaireData: StateFlow<QuestionnaireData> = _questionnaireData

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _analysisResult = MutableStateFlow<AnalysisResult?>(null)
    val analysisResult: StateFlow<AnalysisResult?> = _analysisResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val totalPages = 6

    // Knowledge Base Manager
    private var knowledgeBaseManager: KnowledgeBaseManager? = null

    // ML Model
    private var mlModel: ErgonomicMLModel? = null

    fun setKnowledgeBaseManager(manager: KnowledgeBaseManager) {
        knowledgeBaseManager = manager
    }

    fun setMLModel(context: Context) {
        mlModel = ErgonomicMLModel(context)
    }

    // ========================================================================
    // MÉTODOS DE ACTUALIZACIÓN (sin cambios)
    // ========================================================================

    fun updateChairType(type: ChairType) {
        _questionnaireData.value = _questionnaireData.value.copy(chairType = type)
    }

    fun updateLumbarSupport(support: LumbarSupport) {
        _questionnaireData.value = _questionnaireData.value.copy(lumbarSupport = support)
    }

    fun updateDeskHeight(height: String) {
        _questionnaireData.value = _questionnaireData.value.copy(deskHeight = height)
    }

    fun updateDeskSpace(space: String) {
        _questionnaireData.value = _questionnaireData.value.copy(deskSpace = space)
    }

    fun updateMonitorType(type: String) {
        _questionnaireData.value = _questionnaireData.value.copy(monitorType = type)
    }

    fun updateMonitorHeight(height: MonitorHeight) {
        _questionnaireData.value = _questionnaireData.value.copy(monitorHeight = height)
    }

    fun updateMonitorDistance(distance: String) {
        _questionnaireData.value = _questionnaireData.value.copy(monitorDistance = distance)
    }

    fun updateMultipleMonitors(monitors: String) {
        _questionnaireData.value = _questionnaireData.value.copy(multipleMonitors = monitors)
    }

    fun updateKeyboardPosition(position: String) {
        _questionnaireData.value = _questionnaireData.value.copy(keyboardPosition = position)
    }

    fun updateMouseType(type: String) {
        _questionnaireData.value = _questionnaireData.value.copy(mouseType = type)
    }

    fun updateWristPad(pad: String) {
        _questionnaireData.value = _questionnaireData.value.copy(wristPad = pad)
    }

    fun updateLighting(lighting: String) {
        _questionnaireData.value = _questionnaireData.value.copy(lighting = lighting)
    }

    fun updateScreenGlare(glare: String) {
        _questionnaireData.value = _questionnaireData.value.copy(screenGlare = glare)
    }

    fun updateDeskLamp(lamp: String) {
        _questionnaireData.value = _questionnaireData.value.copy(deskLamp = lamp)
    }

    fun updateTemperature(temp: String) {
        _questionnaireData.value = _questionnaireData.value.copy(temperature = temp)
    }

    fun updateNoiseLevel(noise: String) {
        _questionnaireData.value = _questionnaireData.value.copy(noiseLevel = noise)
    }

    fun updateVentilation(vent: String) {
        _questionnaireData.value = _questionnaireData.value.copy(ventilation = vent)
    }

    fun updatePauseFrequency(frequency: PauseFrequency) {
        _questionnaireData.value = _questionnaireData.value.copy(pauseFrequency = frequency)
    }

    fun updatePauseDuration(duration: String) {
        _questionnaireData.value = _questionnaireData.value.copy(pauseDuration = duration)
    }

    fun updateDoesStretches(stretches: String) {
        _questionnaireData.value = _questionnaireData.value.copy(doesStretches = stretches)
    }

    fun updateContinuousSittingTime(time: String) {
        _questionnaireData.value = _questionnaireData.value.copy(continuousSittingTime = time)
    }

    fun updateWorkHours(hours: Int) {
        _questionnaireData.value = _questionnaireData.value.copy(workHours = hours)
    }

    fun updateAge(age: Int) {
        _questionnaireData.value = _questionnaireData.value.copy(age = age)
    }

    fun updateHasNeckPain(has: Boolean) {
        _questionnaireData.value = _questionnaireData.value.copy(hasNeckPain = has)
    }

    fun updateHasBackPain(has: Boolean) {
        _questionnaireData.value = _questionnaireData.value.copy(hasBackPain = has)
    }

    fun updateHasWristPain(has: Boolean) {
        _questionnaireData.value = _questionnaireData.value.copy(hasWristPain = has)
    }

    // ========================================================================
    // NAVEGACIÓN
    // ========================================================================

    fun nextPage() {
        if (_currentPage.value < totalPages - 1) {
            _currentPage.value++
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
        }
    }

    // ========================================================================
    // ANÁLISIS DEL CUESTIONARIO CON ML
    // ========================================================================

    fun analyzeQuestionnaire() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = analyzeWithMLAndKnowledgeBase(_questionnaireData.value)
                _analysisResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback a análisis con reglas si ML falla
                val result = analyzeWithRules(_questionnaireData.value)
                _analysisResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ANÁLISIS HÍBRIDO: ML + Knowledge Base
     * Combina predicción del modelo con recomendaciones detalladas
     */
    private fun analyzeWithMLAndKnowledgeBase(data: QuestionnaireData): AnalysisResult {

        // 1. PREDICCIÓN CON MODELO ML
        val mlPrediction = mlModel?.predict(data)

        if (mlPrediction != null) {
            println("✅ Predicción ML exitosa:")
            println("   Riesgo: ${mlPrediction.riskLevel} (${mlPrediction.confidence})")
            println("   Áreas: ${mlPrediction.criticalAreas}")
            println("   Síntomas: ${mlPrediction.symptomsProbabilities}")

            // 2. OBTENER RECOMENDACIONES DEL KNOWLEDGE BASE
            val recommendations = knowledgeBaseManager?.generateEnhancedRecommendations(data)
                ?: generateBasicRecommendations(data)

            // 3. CREAR ÁREAS CRÍTICAS
            val criticalAreas = mlPrediction.criticalAreas.mapIndexed { index, area ->
                CriticalArea(
                    name = area,
                    severity = 0.7f + (index * 0.1f),
                    description = getAreaDescription(area, data)
                )
            }

            // 4. CREAR SÍNTOMAS
            val symptoms = mlPrediction.symptomsProbabilities.map { (symptom, prob) ->
                Symptom(
                    name = symptom,
                    probability = prob,
                    description = getSymptomDescription(symptom)
                )
            }

            // 5. COMBINAR TODO
            return AnalysisResult(
                riskLevel = mlPrediction.riskLevel,
                riskScore = mlPrediction.riskScore,
                confidence = mlPrediction.confidence,
                criticalAreas = criticalAreas,
                likelySymptoms = symptoms,
                recommendations = recommendations
            )

        } else {
            // Fallback si ML no está disponible
            println("⚠️ ML no disponible, usando análisis por reglas")
            return analyzeWithRules(data)
        }
    }

    /**
     * Análisis con reglas (fallback)
     */
    private fun analyzeWithRules(data: QuestionnaireData): AnalysisResult {
        var score = 0
        val criticalAreas = mutableListOf<CriticalArea>()
        val symptoms = mutableListOf<Symptom>()

        val recommendations = knowledgeBaseManager?.generateEnhancedRecommendations(data)
            ?: generateBasicRecommendations(data)

        recommendations.forEach { rec ->
            when (rec.priority) {
                "URGENTE", "CRITICO" -> score += 10
                "ALTA" -> score += 5
                "MEDIA" -> score += 3
                else -> score += 1
            }
        }

        recommendations.forEach { rec ->
            if (rec.priority in listOf("URGENTE", "CRITICO", "ALTA")) {
                val severity = when (rec.priority) {
                    "URGENTE", "CRITICO" -> 0.9f
                    "ALTA" -> 0.7f
                    else -> 0.5f
                }
                criticalAreas.add(CriticalArea(
                    name = rec.category,
                    severity = severity,
                    description = rec.title
                ))
            }
        }

        if (data.hasNeckPain) {
            symptoms.add(Symptom("Dolor cervical existente", 1.0f, "Ya presenta dolor de cuello"))
            score += 5
        }
        if (data.hasBackPain) {
            symptoms.add(Symptom("Dolor lumbar existente", 1.0f, "Ya presenta dolor de espalda"))
            score += 5
        }
        if (data.hasWristPain) {
            symptoms.add(Symptom("Dolor de muñecas existente", 1.0f, "Ya presenta dolor en muñecas"))
            score += 4
        }

        val riskLevel = when {
            score >= 30 -> RiskLevel.CRITICO
            score >= 20 -> RiskLevel.ALTO
            score >= 10 -> RiskLevel.MEDIO
            else -> RiskLevel.BAJO
        }

        return AnalysisResult(
            riskLevel = riskLevel,
            riskScore = score,
            confidence = 0.85f,
            criticalAreas = criticalAreas.distinctBy { it.name },
            likelySymptoms = symptoms,
            recommendations = recommendations
        )
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    private fun getAreaDescription(area: String, data: QuestionnaireData): String {
        return when (area) {
            "Silla" -> "Configuración de silla inadecuada para trabajo prolongado"
            "Monitor" -> "Altura o distancia del monitor fuera de rango ergonómico"
            "Pausas" -> "Frecuencia de pausas insuficiente para prevención de TME"
            "Iluminación" -> "Condiciones de iluminación inadecuadas causando fatiga visual"
            else -> "Requiere atención ergonómica"
        }
    }

    private fun getSymptomDescription(symptom: String): String {
        return when (symptom) {
            "Dolor cervical" -> "Tensión en cuello y cervicales por postura o monitor"
            "Dolor lumbar" -> "Molestia en espalda baja por silla o postura prolongada"
            "Dolor de muñecas" -> "Tensión en muñecas por posición de teclado/mouse"
            "Fatiga visual" -> "Cansancio ocular por iluminación o tiempo de pantalla"
            else -> "Síntoma relacionado con ergonomía"
        }
    }

    private fun generateBasicRecommendations(data: QuestionnaireData): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        when (data.chairType) {
            ChairType.INADEQUATE -> {
                recommendations.add(Recommendation(
                    "URGENTE", "Mobiliario",
                    "Cambiar silla inmediatamente",
                    "Esta silla no es apta para trabajo de oficina",
                    listOf(
                        "Usar cojín lumbar temporal",
                        "Planificar compra de silla ergonómica",
                        "Limitar tiempo sentado a 2 horas máximo"
                    )
                ))
            }
            ChairType.BASIC -> {
                recommendations.add(Recommendation(
                    "ALTA", "Mobiliario",
                    "Mejorar silla básica",
                    "Silla sin ajustes limita ergonomía",
                    listOf(
                        "Agregar cojín lumbar ajustable",
                        "Usar apoyapies si es necesario"
                    )
                ))
            }
            else -> {}
        }

        if (data.lumbarSupport == LumbarSupport.NONE && data.workHours >= 6) {
            recommendations.add(Recommendation(
                "ALTA", "Mobiliario",
                "Agregar soporte lumbar",
                "Sin soporte lumbar en jornadas largas",
                listOf("Comprar cojín lumbar ($20-30)", "Usar toalla enrollada temporalmente")
            ))
        }

        when (data.monitorHeight) {
            MonitorHeight.ABOVE_EYES -> {
                recommendations.add(Recommendation(
                    "URGENTE", "Configuración",
                    "Bajar altura del monitor",
                    "Monitor muy alto causa tensión cervical",
                    listOf("Bajar monitor 10-15cm HOY", "Borde superior a nivel de ojos")
                ))
            }
            MonitorHeight.BELOW_15 -> {
                recommendations.add(Recommendation(
                    "ALTA", "Configuración",
                    "Elevar monitor",
                    "Monitor muy bajo fuerza flexión de cuello",
                    listOf("Elevar monitor a altura correcta")
                ))
            }
            else -> {}
        }

        when (data.pauseFrequency) {
            PauseFrequency.NEVER -> {
                recommendations.add(Recommendation(
                    "URGENTE", "Hábitos",
                    "Implementar pausas activas AHORA",
                    "Sin pausas, alto riesgo de TME",
                    listOf(
                        "Configurar alarma cada 45 minutos",
                        "Levantarse y caminar 2-3 minutos"
                    )
                ))
            }
            PauseFrequency.BATHROOM_ONLY -> {
                recommendations.add(Recommendation(
                    "ALTA", "Hábitos",
                    "Aumentar frecuencia de pausas",
                    "Pausas insuficientes",
                    listOf(
                        "Pausas cada 60-90 minutos mínimo",
                        "Incluir estiramientos básicos"
                    )
                ))
            }
            else -> {}
        }

        return recommendations.sortedBy {
            when (it.priority) {
                "URGENTE" -> 0
                "ALTA" -> 1
                "MEDIA" -> 2
                else -> 3
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mlModel?.close()
    }
}