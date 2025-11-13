package com.example.ergonomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ergonomic.ml.ErgonomicMLModel
import com.example.ergonomic.model.*
import com.example.ergonomic.ui.theme.ErgonomicTheme

/**
 * Activity de prueba para validar que el modelo ML funciona correctamente
 * Ejecutar esta Activity antes de probar el flujo completo
 */
class TestMLIntegrationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ErgonomicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestMLScreen()
                }
            }
        }
    }

    @Composable
    fun TestMLScreen() {
        var testResult by remember { mutableStateOf<String>("Esperando prueba...") }
        var isLoading by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isLoading = true
            testResult = runMLTest()
            isLoading = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "🧪 Test de Integración ML",
                style = MaterialTheme.typography.headlineMedium
            )

            if (isLoading) {
                CircularProgressIndicator()
                Text("Ejecutando pruebas...")
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        testResult,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    private fun runMLTest(): String {
        val results = StringBuilder()

        try {
            results.append("🔧 Test 1: Inicialización del modelo\n")
            val mlModel = ErgonomicMLModel(this)
            results.append("✅ Modelo inicializado correctamente\n\n")

            results.append("🔧 Test 2: Caso de riesgo BAJO\n")
            val testDataBajo = QuestionnaireData(
                chairType = ChairType.ERGONOMIC_FULL,
                lumbarSupport = LumbarSupport.ADJUSTABLE,
                deskHeight = "Altura ajustable",
                monitorType = "Monitor externo de escritorio (19\" o más)",
                monitorHeight = MonitorHeight.EYE_LEVEL,
                pauseFrequency = PauseFrequency.EVERY_30_60,
                workHours = 8,
                age = 30,
                hasNeckPain = false,
                hasBackPain = false,
                hasWristPain = false
            )

            val predictionBajo = mlModel.predict(testDataBajo)
            if (predictionBajo != null) {
                results.append("✅ Riesgo predicho: ${predictionBajo.riskLevel}\n")
                results.append("   Confianza: ${(predictionBajo.confidence * 100).toInt()}%\n")
                results.append("   Score: ${predictionBajo.riskScore}\n")

                if (predictionBajo.riskLevel == RiskLevel.BAJO) {
                    results.append("✅ CORRECTO: Se esperaba riesgo BAJO\n\n")
                } else {
                    results.append("⚠️ REVISAR: Se esperaba riesgo BAJO\n\n")
                }
            } else {
                results.append("❌ Error en predicción\n\n")
            }

            results.append("🔧 Test 3: Caso de riesgo CRÍTICO\n")
            val testDataCritico = QuestionnaireData(
                chairType = ChairType.INADEQUATE,
                lumbarSupport = LumbarSupport.NONE,
                deskHeight = "Muy bajo",
                monitorType = "Solo laptop (sin soporte)",
                monitorHeight = MonitorHeight.BELOW_15,
                pauseFrequency = PauseFrequency.NEVER,
                continuousSittingTime = "Más de 3 horas",
                workHours = 12,
                age = 45,
                hasNeckPain = true,
                hasBackPain = true,
                hasWristPain = true
            )

            val predictionCritico = mlModel.predict(testDataCritico)
            if (predictionCritico != null) {
                results.append("✅ Riesgo predicho: ${predictionCritico.riskLevel}\n")
                results.append("   Confianza: ${(predictionCritico.confidence * 100).toInt()}%\n")
                results.append("   Score: ${predictionCritico.riskScore}\n")
                results.append("   Áreas críticas: ${predictionCritico.criticalAreas.joinToString()}\n")

                if (predictionCritico.riskLevel in listOf(RiskLevel.ALTO, RiskLevel.CRITICO)) {
                    results.append("✅ CORRECTO: Se esperaba riesgo ALTO/CRÍTICO\n\n")
                } else {
                    results.append("⚠️ REVISAR: Se esperaba riesgo ALTO/CRÍTICO\n\n")
                }
            } else {
                results.append("❌ Error en predicción\n\n")
            }

            results.append("🔧 Test 4: Verificar síntomas\n")
            if (predictionCritico?.symptomsProbabilities?.isNotEmpty() == true) {
                results.append("✅ Síntomas detectados:\n")
                predictionCritico.symptomsProbabilities.forEach { (symptom, prob) ->
                    results.append("   • $symptom: ${(prob * 100).toInt()}%\n")
                }
            }

            mlModel.close()
            results.append("\n🎉 TODOS LOS TESTS COMPLETADOS")

        } catch (e: Exception) {
            results.append("\n❌ ERROR: ${e.message}\n")
            results.append("Stack trace: ${e.stackTraceToString()}")
        }

        return results.toString()
    }
}