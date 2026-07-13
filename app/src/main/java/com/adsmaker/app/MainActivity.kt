package com.adsmaker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adsmaker.app.ui.create.CreateAdScreen
import com.adsmaker.app.ui.create.CreateAdViewModel
import com.adsmaker.app.ui.preview.PreviewScreen
import com.adsmaker.app.ui.settings.SettingsScreen
import com.adsmaker.app.ui.settings.SettingsViewModel
import com.adsmaker.app.ui.theme.AdsMakerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AdsMakerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AdsMakerApp()
                }
            }
        }
    }
}

private object Routes {
    const val CREATE = "create"
    const val PREVIEW = "preview"
    const val SETTINGS = "settings"
}

@androidx.compose.runtime.Composable
private fun AdsMakerApp() {
    val navController = rememberNavController()
    // One activity-scoped ViewModel shared by both screens.
    val viewModel: CreateAdViewModel = viewModel(factory = CreateAdViewModel.factory())
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.CREATE) {
        composable(Routes.CREATE) {
            CreateAdScreen(
                viewModel = viewModel,
                onGenerated = {
                    navController.navigate(Routes.PREVIEW) {
                        launchSingleTop = true
                    }
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory())
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = {
                    // Re-read key + owner mode so the create screen reflects changes.
                    viewModel.refreshSettingsState()
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.PREVIEW) {
            val ad = state.generatedAd
            if (ad == null) {
                // Guard against a direct/rotated entry with no result yet.
                navController.popBackStack(Routes.CREATE, inclusive = false)
            } else {
                PreviewScreen(
                    ad = ad,
                    onBack = { navController.popBackStack() },
                    onRegenerate = {
                        viewModel.prepareForRegenerate()
                        navController.popBackStack(Routes.CREATE, inclusive = false)
                    },
                )
            }
        }
    }
}
