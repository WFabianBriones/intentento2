package com.example.ergonomic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ergonomic.ui.pages.*
import com.example.ergonomic.viewmodel.QuestionnaireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    viewModel: QuestionnaireViewModel,
    onNavigateToResults: () -> Unit
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val questionnaireData by viewModel.questionnaireData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluación Ergonómica") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Indicador de progreso
            LinearProgressIndicator(
                progress = { (currentPage + 1).toFloat() / viewModel.totalPages },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Página ${currentPage + 1} de ${viewModel.totalPages}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Contenido de la página actual
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentPage) {
                    0 -> MobiliarioPage(viewModel, questionnaireData)
                    1 -> MonitorPage(viewModel, questionnaireData)
                    2 -> TecladoMousePage(viewModel, questionnaireData)
                    3 -> IluminacionPage(viewModel, questionnaireData)
                    4 -> PausasPage(viewModel, questionnaireData)
                    5 -> DatosPersonalesPage(viewModel, questionnaireData)
                }
            }

            // Botones de navegación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousPage() }
                    ) {
                        Text("Anterior")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentPage < viewModel.totalPages - 1) {
                    Button(
                        onClick = { viewModel.nextPage() }
                    ) {
                        Text("Siguiente")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.analyzeQuestionnaire()
                            onNavigateToResults()
                        }
                    ) {
                        Text("Ver Resultados")
                    }
                }
            }
        }
    }
}