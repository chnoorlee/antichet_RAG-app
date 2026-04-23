package com.antifraud.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antifraud.app.ui.*
import com.antifraud.app.ui.theme.AntifraudTheme
import com.antifraud.app.viewmodel.AnalysisViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AntifraudTheme {
                val navController = rememberNavController()
                val viewModel: AnalysisViewModel = viewModel()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToResult = { navController.navigate("result") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToHistory = { navController.navigate("history") }
                        )
                    }
                    composable("result") {
                        ResultScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
