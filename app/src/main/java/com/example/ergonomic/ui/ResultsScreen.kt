package com.example.ergonomic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ergonomic.model.*
import com.example.ergonomic.viewmodel.QuestionnaireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: QuestionnaireViewModel,
    onNavigateBack: () -> Unit
) {
    val analysisResult by viewModel.analysisResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados del Análisis") }
            )
        }
    ) { paddingValues ->
        analysisResult?.let { result ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Card de nivel de riesgo
                RiskLevelCard(result.riskLevel, result.riskScore)

                Spacer(modifier = Modifier.height(24.dp))

                // Áreas críticas
                if (result.criticalAreas.isNotEmpty()) {
                    SectionTitle("Áreas Críticas")
                    result.criticalAreas.forEach { area ->
                        CriticalAreaCard(area)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Síntomas probables
                if (result.likelySymptoms.isNotEmpty()) {
                    SectionTitle("Síntomas Probables")
                    result.likelySymptoms.forEach { symptom ->
                        SymptomCard(symptom)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Recomendaciones
                SectionTitle("Recomendaciones Prioritarias")
                result.recommendations.forEach { recommendation ->
                    RecommendationCard(recommendation)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al Inicio")
                }
            }
        }
    }
}

@Composable
fun RiskLevelCard(level: RiskLevel, score: Int) {
    val (color, emoji) = when (level) {
        RiskLevel.BAJO -> Color(0xFF4CAF50) to "✅"
        RiskLevel.MEDIO -> Color(0xFFFFC107) to "⚠️"
        RiskLevel.ALTO -> Color(0xFFFF9800) to "🔶"
        RiskLevel.CRITICO -> Color(0xFFF44336) to "🚨"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$emoji Nivel de Riesgo: ${level.name}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "Puntuación: $score",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun CriticalAreaCard(area: CriticalArea) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = area.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = area.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Severidad: ${(area.severity * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SymptomCard(symptom: Symptom) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔴",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = symptom.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = symptom.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Probabilidad: ${(symptom.probability * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color(0xFFFF6F00),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: Recommendation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                val priorityColor = when(recommendation.priority) {
                    "URGENTE" -> Color.Red
                    "ALTA" -> Color(0xFFFF9800)
                    "MEDIA" -> Color(0xFFFFC107)
                    else -> Color.Gray
                }

                Surface(
                    color = priorityColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = recommendation.priority,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = recommendation.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = recommendation.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = recommendation.description,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            recommendation.actions.forEach { action ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = "• ", fontWeight = FontWeight.Bold)
                    Text(text = action, fontSize = 14.sp)
                }
            }
        }
    }
}