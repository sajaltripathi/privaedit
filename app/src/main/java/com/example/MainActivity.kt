package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.DocumentRepository
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.Theme // Adjusted to generic theme import based on your repo structure

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize local offline Room Database and Repository safely
        val database = AppDatabase.getDatabase(this)
        val repository = DocumentRepository(this, database.documentDao())

        // 2. Instantiate the ViewModel with Factory
        val viewModel = ViewModelProvider(
            this,
            DocumentViewModelFactory(repository)
        )[DocumentViewModel::class.java]

        setContent {
            // Use your app's actual theme here
            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                MainScreenContent(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun MainScreenContent(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier
) {
    // Elegant Crossfade navigation for modern feeling transitions
    Crossfade(
        targetState = viewModel.currentScreen,
        modifier = modifier.fillMaxSize(),
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.DASHBOARD -> {
                DashboardScreen(viewModel = viewModel)
            }
            AppScreen.IMAGE_EDITOR -> {
                ImageEditorScreen(viewModel = viewModel)
            }
            AppScreen.TEXT_EDITOR -> {
                TextEditorScreen(viewModel = viewModel)
            }
            AppScreen.PDF_EDITOR -> {
                PdfEditorScreen(viewModel = viewModel)
            }
            AppScreen.SECURITY_SETTINGS -> {
                SecuritySettingsScreen(viewModel = viewModel)
            }
        }
    }
}
