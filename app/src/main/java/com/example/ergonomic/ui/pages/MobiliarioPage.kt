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
fun MobiliarioPage(
    viewModel: QuestionnaireViewModel,
    data: QuestionnaireData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionTitle("Mobiliario y Equipamiento")

        QuestionTitle("1. Tipo de silla que utilizas")
        ChairType.values().forEach { type ->
            RadioOption(
                text = type.value,
                selected = data.chairType == type,
                onSelect = { viewModel.updateChairType(type) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        QuestionTitle("2. ¿Tu silla tiene soporte lumbar?")
        LumbarSupport.values().forEach { support ->
            RadioOption(
                text = support.value,
                selected = data.lumbarSupport == support,
                onSelect = { viewModel.updateLumbarSupport(support) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        QuestionTitle("3. Altura del escritorio/mesa")
        val deskHeightOptions = listOf(
            "Altura ajustable",
            "Altura adecuada para mi estatura",
            "Muy alto",
            "Muy bajo",
            "No tengo escritorio dedicado"
        )
        deskHeightOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.deskHeight == option,
                onSelect = { viewModel.updateDeskHeight(option) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        QuestionTitle("4. Espacio en el escritorio")
        val deskSpaceOptions = listOf(
            "Amplio (puedo extender brazos)",
            "Adecuado",
            "Limitado",
            "Muy reducido"
        )
        deskSpaceOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.deskSpace == option,
                onSelect = { viewModel.updateDeskSpace(option) }
            )
        }
    }
}