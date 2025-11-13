package com.example.ergonomic.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ergonomic.model.QuestionnaireData
import com.example.ergonomic.ui.*
import com.example.ergonomic.viewmodel.QuestionnaireViewModel

@Composable
fun DatosPersonalesPage(
    viewModel: QuestionnaireViewModel,
    data: QuestionnaireData
) {
    var workHours by remember { mutableFloatStateOf(data.workHours.toFloat()) }
    var age by remember { mutableFloatStateOf(data.age.toFloat()) }
    var hasNeckPain by remember { mutableStateOf(data.hasNeckPain) }
    var hasBackPain by remember { mutableStateOf(data.hasBackPain) }
    var hasWristPain by remember { mutableStateOf(data.hasWristPain) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionTitle("Datos Personales y Síntomas")

        QuestionTitle("Horas de trabajo diarias: ${workHours.toInt()}h")
        Slider(
            value = workHours,
            onValueChange = {
                workHours = it
                viewModel.updateWorkHours(it.toInt())
            },
            valueRange = 1f..16f,
            steps = 14,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Desliza para ajustar (1-16 horas)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Edad: ${age.toInt()} años")
        Slider(
            value = age,
            onValueChange = {
                age = it
                viewModel.updateAge(it.toInt())
            },
            valueRange = 18f..70f,
            steps = 51,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Desliza para ajustar (18-70 años)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        QuestionTitle("¿Experimentas alguno de estos síntomas actualmente?")

        CheckboxOption(
            text = "Dolor de cuello o cervical",
            checked = hasNeckPain,
            onCheckedChange = {
                hasNeckPain = it
                viewModel.updateHasNeckPain(it)
            }
        )

        CheckboxOption(
            text = "Dolor de espalda o lumbar",
            checked = hasBackPain,
            onCheckedChange = {
                hasBackPain = it
                viewModel.updateHasBackPain(it)
            }
        )

        CheckboxOption(
            text = "Dolor de muñecas o manos",
            checked = hasWristPain,
            onCheckedChange = {
                hasWristPain = it
                viewModel.updateHasWristPain(it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 Información importante",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Los datos que proporcionas son anónimos y se usan únicamente para generar tu evaluación ergonómica personalizada.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}