package com.example.ergonomic.ml

import android.content.Context
import com.example.ergonomic.model.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Clase para ejecutar inferencia con el modelo TensorFlow Lite
 */
class ErgonomicMLModel(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val inputSize = 26

    // Normalización (VALORES EXACTOS del modelo entrenado)
    // Extraídos de model_metadata.json - Accuracy: 91.00%
    // Fecha: 2025-11-12 23:00:45
    private val scalerMean = floatArrayOf(
        0.341588f, 0.396647f, 0.416588f, 0.382902f,  // mobiliario (4)
        0.312794f, 0.409353f, 0.331020f, 0.232176f,  // monitor (4)
        0.435294f, 0.488627f, 0.603922f, 0.419929f,  // teclado/mouse (3)
        0.366314f, 0.599824f, 0.299137f, 0.381373f,  // iluminación (6)
        0.369569f, 0.423471f, 0.374706f, 0.413961f,  // iluminación cont.
        0.502353f, 0.530301f, 0.301209f, 0.297294f,  // pausas (4)
        0.292588f, 0.253294f  // personales (2)
    )

    private val scalerScale = floatArrayOf(
        0.310445f, 0.402339f, 0.257473f, 0.264007f,  // mobiliario (4)
        0.285440f, 0.339187f, 0.332560f, 0.297648f,  // monitor (4)
        0.349990f, 0.293260f, 0.358589f, 0.304094f,  // teclado/mouse (3)
        0.316223f, 0.435964f, 0.350173f, 0.265400f,  // iluminación (6)
        0.272715f, 0.285166f, 0.328595f, 0.326532f,  // iluminación cont.
        0.309217f, 0.079893f, 0.211716f, 0.457067f,  // pausas (4)
        0.454951f, 0.434898f  // personales (2)
    )

    init {
        loadModel()
    }

    /**
     * Cargar modelo TFLite desde assets
     */
    private fun loadModel() {
        try {
            val modelFile = loadModelFile("ergonomic_model.tflite")

            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseXNNPACK(true)  // Aceleración
            }

            interpreter = Interpreter(modelFile, options)

            // Verificar dimensiones
            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val outputShape0 = interpreter?.getOutputTensor(0)?.shape()

            println("✅ Modelo ML cargado exitosamente")
            println("   Input: ${inputShape?.contentToString()}")
            println("   Output 0: ${outputShape0?.contentToString()}")

        } catch (e: Exception) {
            println("❌ Error cargando modelo ML: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Cargar archivo .tflite desde assets
     */
    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Ejecutar predicción del modelo
     */
    fun predict(data: QuestionnaireData): MLPrediction? {
        if (interpreter == null) {
            println("❌ Modelo no inicializado")
            return null
        }

        try {
            // 1. Preparar input
            val inputArray = prepareInput(data)

            // 2. Crear buffers de output
            val riskOutput = Array(1) { FloatArray(4) }  // [BAJO, MEDIO, ALTO, CRITICO]
            val areasOutput = Array(1) { FloatArray(4) }  // [chair, monitor, breaks, lighting]
            val symptomsOutput = Array(1) { FloatArray(4) }  // [neck, back, wrist, eye]

            // 3. Ejecutar inferencia
            val outputs = mapOf(
                0 to riskOutput,
                1 to areasOutput,
                2 to symptomsOutput
            )

            interpreter?.runForMultipleInputsOutputs(arrayOf(inputArray), outputs)

            // 4. Procesar resultados
            return processPrediction(riskOutput[0], areasOutput[0], symptomsOutput[0])

        } catch (e: Exception) {
            println("❌ Error en predicción: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    /**
     * Preparar array de input normalizado
     */
    private fun prepareInput(data: QuestionnaireData): Array<FloatArray> {
        val features = FloatArray(inputSize)

        var idx = 0

        // MOBILIARIO (4)
        features[idx++] = data.chairType?.ordinal?.toFloat() ?: 2f
        features[idx++] = data.lumbarSupport?.ordinal?.toFloat() ?: 1f
        features[idx++] = mapDeskHeight(data.deskHeight)
        features[idx++] = mapDeskSpace(data.deskSpace)

        // MONITOR (4)
        features[idx++] = mapMonitorType(data.monitorType)
        features[idx++] = data.monitorHeight?.ordinal?.toFloat() ?: 2f
        features[idx++] = mapMonitorDistance(data.monitorDistance)
        features[idx++] = mapMultipleMonitors(data.multipleMonitors)

        // TECLADO Y MOUSE (3)
        features[idx++] = mapKeyboardPosition(data.keyboardPosition)
        features[idx++] = mapMouseType(data.mouseType)
        features[idx++] = mapWristPad(data.wristPad)

        // ILUMINACIÓN (6)
        features[idx++] = mapLighting(data.lighting)
        features[idx++] = mapScreenGlare(data.screenGlare)
        features[idx++] = mapDeskLamp(data.deskLamp)
        features[idx++] = mapTemperature(data.temperature)
        features[idx++] = mapNoiseLevel(data.noiseLevel)
        features[idx++] = mapVentilation(data.ventilation)

        // PAUSAS (4)
        features[idx++] = data.pauseFrequency?.ordinal?.toFloat() ?: 2f
        features[idx++] = mapPauseDuration(data.pauseDuration)
        features[idx++] = mapDoesStretches(data.doesStretches)
        features[idx++] = mapContinuousSitting(data.continuousSittingTime)

        // DATOS PERSONALES (5)
        features[idx++] = data.workHours / 16f  // Normalizar 0-1
        features[idx++] = (data.age - 18) / 52f  // Normalizar 0-1
        features[idx++] = if (data.hasNeckPain) 1f else 0f
        features[idx++] = if (data.hasBackPain) 1f else 0f
        features[idx++] = if (data.hasWristPain) 1f else 0f

        // Normalizar con StandardScaler
        for (i in features.indices) {
            features[i] = (features[i] - scalerMean[i]) / scalerScale[i]
        }

        return arrayOf(features)
    }

    /**
     * Procesar outputs del modelo
     */
    private fun processPrediction(
        riskProbs: FloatArray,
        areasProbs: FloatArray,
        symptomsProbs: FloatArray
    ): MLPrediction {

        // 1. Determinar nivel de riesgo
        val riskIndex = riskProbs.indices.maxByOrNull { riskProbs[it] } ?: 0
        val riskLevel = when (riskIndex) {
            0 -> RiskLevel.BAJO
            1 -> RiskLevel.MEDIO
            2 -> RiskLevel.ALTO
            else -> RiskLevel.CRITICO
        }
        val confidence = riskProbs[riskIndex]

        // 2. Áreas críticas (threshold > 0.5)
        val criticalAreas = mutableListOf<String>()
        val areaNames = listOf("Silla", "Monitor", "Pausas", "Iluminación")
        areasProbs.forEachIndexed { index, prob ->
            if (prob > 0.5f) {
                criticalAreas.add(areaNames[index])
            }
        }

        // 3. Síntomas probables (threshold > 0.4)
        val symptoms = mutableListOf<Pair<String, Float>>()
        val symptomNames = listOf(
            "Dolor cervical",
            "Dolor lumbar",
            "Dolor de muñecas",
            "Fatiga visual"
        )
        symptomsProbs.forEachIndexed { index, prob ->
            if (prob > 0.4f) {
                symptoms.add(symptomNames[index] to prob)
            }
        }

        // 4. Calcular risk score aproximado
        val riskScore = (riskIndex * 10 + confidence * 10).toInt()

        return MLPrediction(
            riskLevel = riskLevel,
            confidence = confidence,
            riskScore = riskScore,
            criticalAreas = criticalAreas,
            symptomsProbabilities = symptoms
        )
    }

    // ========== FUNCIONES DE MAPEO ==========

    private fun mapDeskHeight(value: String?): Float {
        return when (value) {
            "Altura ajustable" -> 0f
            "Altura adecuada para mi estatura" -> 1f
            "Muy alto" -> 2f
            "Muy bajo" -> 3f
            "No tengo escritorio dedicado" -> 4f
            else -> 2f
        }
    }

    private fun mapDeskSpace(value: String?): Float {
        return when (value) {
            "Amplio (puedo extender brazos)" -> 0f
            "Adecuado" -> 1f
            "Limitado" -> 2f
            "Muy reducido" -> 3f
            else -> 1f
        }
    }

    private fun mapMonitorType(value: String?): Float {
        return when (value) {
            "Monitor externo de escritorio (19\" o más)" -> 0f
            "Laptop con monitor externo adicional" -> 1f
            "Solo laptop (con soporte elevado)" -> 2f
            "Solo laptop (sin soporte)" -> 3f
            "Tablet/iPad" -> 4f
            else -> 2f
        }
    }

    private fun mapMonitorDistance(value: String?): Float {
        return when (value) {
            "50-70 cm (longitud de brazo) - correcto" -> 0f
            "Menos de 50 cm (muy cerca)" -> 1f
            "Más de 70 cm (muy lejos)" -> 2f
            "No sé/No lo he medido" -> 3f
            else -> 1f
        }
    }

    private fun mapMultipleMonitors(value: String?): Float {
        return when (value) {
            "No, solo uno" -> 0f
            "Sí, dos monitores" -> 1f
            "Sí, tres o más" -> 2f
            else -> 0f
        }
    }

    private fun mapKeyboardPosition(value: String?): Float {
        return when (value) {
            "A la altura de los codos (codos en 90°)" -> 0f
            "Por encima de los codos" -> 1f
            "Por debajo de los codos" -> 2f
            "Varía constantemente" -> 3f
            else -> 1f
        }
    }

    private fun mapMouseType(value: String?): Float {
        return when (value) {
            "Mouse ergonómico vertical" -> 0f
            "Mouse estándar" -> 1f
            "Trackpad de laptop" -> 2f
            "Mouse inalámbrico estándar" -> 3f
            else -> 1f
        }
    }

    private fun mapWristPad(value: String?): Float {
        return when (value) {
            "Sí, para teclado y mouse" -> 0f
            "Sí, solo para mouse" -> 1f
            "Sí, solo para teclado" -> 2f
            "No uso" -> 3f
            else -> 3f
        }
    }

    private fun mapLighting(value: String?): Float {
        return when (value) {
            "Luz natural abundante (ventana grande)" -> 0f
            "Luz natural moderada" -> 1f
            "Luz artificial (LED blanco/neutro)" -> 2f
            "Luz artificial (amarilla/cálida)" -> 3f
            "Mezcla de natural y artificial" -> 4f
            "Insuficiente/Tenue" -> 5f
            else -> 2f
        }
    }

    private fun mapScreenGlare(value: String?): Float {
        return when (value) {
            "Nunca" -> 0f
            "Ocasionalmente" -> 1f
            "Frecuentemente" -> 2f
            "Constantemente" -> 3f
            else -> 1f
        }
    }

    private fun mapDeskLamp(value: String?): Float {
        return when (value) {
            "Sí, ajustable" -> 0f
            "Sí, fija" -> 1f
            "No" -> 2f
            else -> 1f
        }
    }

    private fun mapTemperature(value: String?): Float {
        return when (value) {
            "Confortable" -> 0f
            "Frío frecuentemente" -> 1f
            "Calor frecuentemente" -> 2f
            "Varía mucho" -> 3f
            else -> 0f
        }
    }

    private fun mapNoiseLevel(value: String?): Float {
        return when (value) {
            "Silencioso" -> 0f
            "Ruido moderado" -> 1f
            "Ruidoso" -> 2f
            "Muy ruidoso" -> 3f
            else -> 1f
        }
    }

    private fun mapVentilation(value: String?): Float {
        return when (value) {
            "Excelente (aire fresco)" -> 0f
            "Buena" -> 1f
            "Regular (algo cargado)" -> 2f
            "Mala (aire viciado)" -> 3f
            else -> 1f
        }
    }

    private fun mapPauseDuration(value: String?): Float {
        return when (value) {
            "5-10 minutos" -> 0f
            "2-5 minutos" -> 1f
            "Menos de 2 minutos" -> 2f
            "No hago pausas" -> 3f
            else -> 1f
        }
    }

    private fun mapDoesStretches(value: String?): Float {
        return when (value) {
            "Sí, siempre" -> 0f
            "A veces" -> 1f
            "Rara vez" -> 2f
            "Nunca" -> 3f
            else -> 2f
        }
    }

    private fun mapContinuousSitting(value: String?): Float {
        return when (value) {
            "Menos de 1 hora" -> 0f
            "1-2 horas" -> 1f
            "2-3 horas" -> 2f
            "Más de 3 horas" -> 3f
            else -> 2f
        }
    }

    /**
     * Liberar recursos
     */
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

/**
 * Resultado de la predicción del modelo ML
 */
data class MLPrediction(
    val riskLevel: RiskLevel,
    val confidence: Float,
    val riskScore: Int,
    val criticalAreas: List<String>,
    val symptomsProbabilities: List<Pair<String, Float>>
)