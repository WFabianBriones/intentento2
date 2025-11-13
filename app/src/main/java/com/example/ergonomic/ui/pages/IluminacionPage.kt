package com.example.ergonomic.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ergonomic.model.QuestionnaireData
import com.example.ergonomic.ui.*
import com.example.ergonomic.viewmodel.QuestionnaireViewModel

@Composable
fun IluminacionPage(
    viewModel: QuestionnaireViewModel,
    data: QuestionnaireData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionTitle("Iluminación y Ambiente")

        QuestionTitle("Iluminación principal del espacio")
        val lightingOptions = listOf(
            "Luz natural abundante (ventana grande)",
            "Luz natural moderada",
            "Luz artificial (LED blanco/neutro)",
            "Luz artificial (amarilla/cálida)",
            "Mezcla de natural y artificial",
            "Insuficiente/Tenue"
        )
        lightingOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.lighting == option,
                onSelect = { viewModel.updateLighting(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("¿Hay reflejos en tu pantalla?")
        val glareOptions = listOf("Nunca", "Ocasionalmente", "Frecuentemente", "Constantemente")
        glareOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.screenGlare == option,
                onSelect = { viewModel.updateScreenGlare(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("¿Usas lámpara de escritorio adicional?")
        val lampOptions = listOf("Sí, ajustable", "Sí, fija", "No")
        lampOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.deskLamp == option,
                onSelect = { viewModel.updateDeskLamp(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Temperatura del espacio de trabajo")
        val tempOptions = listOf(
            "Confortable",
            "Frío frecuentemente",
            "Calor frecuentemente",
            "Varía mucho"
        )
        tempOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.temperature == option,
                onSelect = { viewModel.updateTemperature(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Nivel de ruido")
        val noiseOptions = listOf("Silencioso", "Ruido moderado", "Ruidoso", "Muy ruidoso")
        noiseOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.noiseLevel == option,
                onSelect = { viewModel.updateNoiseLevel(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Ventilación del espacio")
        val ventOptions = listOf(
            "Excelente (aire fresco)",
            "Buena",
            "Regular (algo cargado)",
            "Mala (aire viciado)"
        )
        ventOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.ventilation == option,
                onSelect = { viewModel.updateVentilation(option) }
            )
        }
    }
}