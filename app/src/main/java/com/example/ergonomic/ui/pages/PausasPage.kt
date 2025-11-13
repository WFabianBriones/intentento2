package com.example.ergonomic.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ergonomic.model.*
import com.example.ergonomic.ui.*
import com.example.ergonomic.viewmodel.QuestionnaireViewModel

@Composable
fun PausasPage(
    viewModel: QuestionnaireViewModel,
    data: QuestionnaireData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionTitle("Pausas y Movimiento")

        QuestionTitle("¿Realizas pausas activas durante tu jornada?")
        PauseFrequency.values().forEach { frequency ->
            RadioOption(
                text = frequency.value,
                selected = data.pauseFrequency == frequency,
                onSelect = { viewModel.updatePauseFrequency(frequency) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Duración típica de las pausas")
        val durationOptions = listOf(
            "5-10 minutos",
            "2-5 minutos",
            "Menos de 2 minutos",
            "No hago pausas"
        )
        durationOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.pauseDuration == option,
                onSelect = { viewModel.updatePauseDuration(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Durante las pausas, ¿realizas estiramientos?")
        val stretchOptions = listOf("Sí, siempre", "A veces", "Rara vez", "Nunca")
        stretchOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.doesStretches == option,
                onSelect = { viewModel.updateDoesStretches(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Tiempo promedio continuo sentado sin levantarte")
        val sittingOptions = listOf(
            "Menos de 1 hora",
            "1-2 horas",
            "2-3 horas",
            "Más de 3 horas"
        )
        sittingOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.continuousSittingTime == option,
                onSelect = { viewModel.updateContinuousSittingTime(option) }
            )
        }
    }
}