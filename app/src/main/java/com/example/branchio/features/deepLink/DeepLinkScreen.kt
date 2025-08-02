package com.example.branchio.features.deepLink

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeepLinkScreen(
    navController: NavController,
    intent: Intent,
    viewModel: DeepLinkViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val effectFlow = viewModel.effect

    // Trigger deep link handling once
    LaunchedEffect(Unit) {
        viewModel.onEvent(DeepLinkEvent.HandleDeepLinkIntent(intent))
    }

    // Handle effects
    LaunchedEffect(effectFlow) {
        effectFlow.collect { effect ->
            when (effect) {
                is DeepLinkEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }

                DeepLinkEffect.NaviagteToHome -> navController.popBackStack()
            }
        }
    }

    // Render UI based on state
    when (val state = viewState) {
        is DeepLinkState.Idle -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Waiting for deep link...")
            }
        }

        is DeepLinkState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is DeepLinkState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Deep Link Data", style = MaterialTheme.typography.titleLarge)
                Text("Title: ${state.data.title}")
                Text("Description: ${state.data.description}")
                Text("Image URL: ${state.data.imageUrl}")
                Text("Metadata:")
                state.data.metadata.forEach { (key, value) ->
                    Text("• $key: $value")
                }
            }
        }

        is DeepLinkState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
