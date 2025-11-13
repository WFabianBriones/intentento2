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
fun MonitorPage(
    viewModel: QuestionnaireViewModel,
    data: QuestionnaireData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionTitle("Configuración del Monitor")

        QuestionTitle("Tipo de monitor principal")
        val monitorTypeOptions = listOf(
            "Monitor externo de escritorio (19\" o más)",
            "Laptop con monitor externo adicional",
            "Solo laptop (con soporte elevado)",
            "Solo laptop (sin soporte)",
            "Tablet/iPad"
        )
        monitorTypeOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.monitorType == option,
                onSelect = { viewModel.updateMonitorType(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Altura del monitor respecto a tus ojos")
        MonitorHeight.values().forEach { height ->
            RadioOption(
                text = height.value,
                selected = data.monitorHeight == height,
                onSelect = { viewModel.updateMonitorHeight(height) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Distancia del monitor a tus ojos")
        val distanceOptions = listOf(
            "50-70 cm (longitud de brazo) - correcto",
            "Menos de 50 cm (muy cerca)",
            "Más de 70 cm (muy lejos)",
            "No sé/No lo he medido"
        )
        distanceOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.monitorDistance == option,
                onSelect = { viewModel.updateMonitorDistance(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("¿Usas más de un monitor?")
        val multiMonitorOptions = listOf(
            "No, solo uno",
            "Sí, dos monitores",
            "Sí, tres o más"
        )
        multiMonitorOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.multipleMonitors == option,
                onSelect = { viewModel.updateMultipleMonitors(option) }
            )
        }
    }
}