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
fun TecladoMousePage(
    viewModel: QuestionnaireViewModel,
    data: QuestionnaireData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionTitle("Teclado y Mouse")

        QuestionTitle("Posición del teclado respecto a tus codos")
        val keyboardOptions = listOf(
            "A la altura de los codos (codos en 90°)",
            "Por encima de los codos",
            "Por debajo de los codos",
            "Varía constantemente"
        )
        keyboardOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.keyboardPosition == option,
                onSelect = { viewModel.updateKeyboardPosition(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("Tipo de mouse")
        val mouseOptions = listOf(
            "Mouse ergonómico vertical",
            "Mouse estándar",
            "Trackpad de laptop",
            "Mouse inalámbrico estándar"
        )
        mouseOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.mouseType == option,
                onSelect = { viewModel.updateMouseType(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        QuestionTitle("¿Usas almohadilla de apoyo para muñeca?")
        val wristPadOptions = listOf(
            "Sí, para teclado y mouse",
            "Sí, solo para mouse",
            "Sí, solo para teclado",
            "No uso"
        )
        wristPadOptions.forEach { option ->
            RadioOption(
                text = option,
                selected = data.wristPad == option,
                onSelect = { viewModel.updateWristPad(option) }
            )
        }
    }
}