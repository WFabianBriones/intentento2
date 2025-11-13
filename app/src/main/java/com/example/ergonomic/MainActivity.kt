package com.example.ergonomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ergonomic.data.KnowledgeBaseManager
import com.example.ergonomic.ui.QuestionnaireScreen
import com.example.ergonomic.ui.ResultsScreen
import com.example.ergonomic.ui.WelcomeScreen
import com.example.ergonomic.ui.theme.ErgonomicTheme
import com.example.ergonomic.viewmodel.QuestionnaireViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ErgonomicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ErgonomicApp()
                }
            }
        }
    }
}

@Composable
fun ErgonomicApp() {
    val navController = rememberNavController()
    val viewModel: QuestionnaireViewModel = viewModel()
    val context = LocalContext.current

    // Inicializar Knowledge Base Manager y ML Model
    LaunchedEffect(Unit) {
        try {
            // 1. Cargar Knowledge Base
            val kbManager = KnowledgeBaseManager(context)
            viewModel.setKnowledgeBaseManager(kbManager)
            println("✅ Knowledge Base cargado")

            // 2. Cargar Modelo ML
            viewModel.setMLModel(context)
            println("✅ Modelo ML inicializado")

        } catch (e: Exception) {
            println("❌ Error inicializando recursos: ${e.message}")
            e.printStackTrace()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onStartQuestionnaire = {
                    navController.navigate("questionnaire")
                }
            )
        }

        composable("questionnaire") {
            QuestionnaireScreen(
                viewModel = viewModel,
                onNavigateToResults = {
                    navController.navigate("results") {
                        // Evitar volver al cuestionario con back button
                        popUpTo("welcome") { inclusive = false }
                    }
                }
            )
        }

        composable("results") {
            ResultsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack("welcome", inclusive = false)
                }
            )
        }
    }
}