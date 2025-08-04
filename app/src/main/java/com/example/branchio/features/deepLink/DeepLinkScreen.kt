package com.example.branchio.features.deepLink

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.branchio.util.BranchLinkExtractor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepLinkScreen(
    navController: NavController,
    intent: Intent,
    viewModel: DeepLinkViewModel,
    deepLinkPath: String,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val effectFlow = viewModel.effect

    LaunchedEffect(activity) {
        activity?.let { viewModel.setActivity(it) }
    }

    // Trigger deep link handling once when screen loads
    LaunchedEffect(key1 = deepLinkPath, key2 = intent.data.toString()) {
        viewModel.onEvent(DeepLinkEvent.HandleDeepLinkIntent(intent))
    }

    // Handle effects
    LaunchedEffect(effectFlow) {
        effectFlow.collect { effect ->
            when (effect) {
                is DeepLinkEffect.ShowError -> {
                    Toast.makeText(context, "Error: ${effect.message}", Toast.LENGTH_LONG).show()
                }
                is DeepLinkEffect.ShowSuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is DeepLinkEffect.ShowInfo -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                DeepLinkEffect.NavigateToHome -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // Main UI
    Box(
        modifier = Modifier.systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Deep Link Handler",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Path: $deepLinkPath",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "URI: ${intent.data}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on state
            when (val state = viewState) {
                is DeepLinkState.Idle -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Initializing deep link processing...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                is DeepLinkState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Processing deep link...",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                is DeepLinkState.Success -> {
                    DeepLinkContentView(
                        branchLinkData = state.data,
                        onNavigateHome = {
                            viewModel.onEvent(DeepLinkEvent.ClearError)
                            navController.popBackStack()
                        }
                    )
                }

                is DeepLinkState.Error -> {
                    DeepLinkErrorView(
                        errorMessage = state.message,
                        onRetry = { viewModel.onEvent(DeepLinkEvent.RetryDeepLink(intent)) },
                        onNavigateHome = {
                            viewModel.onEvent(DeepLinkEvent.ClearError)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeepLinkContentView(
    branchLinkData: com.example.branchio.domain.entity.BranchLinkData,
    onNavigateHome: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Deep Link Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (BranchLinkExtractor.hasValidContent(branchLinkData)) {
                // Show content sections
                if (branchLinkData.title.isNotEmpty()) {
                    ContentSection(label = "Title", value = branchLinkData.title)
                }

                if (branchLinkData.description.isNotEmpty()) {
                    ContentSection(label = "Description", value = branchLinkData.description)
                }

                if (branchLinkData.imageUrl.isNotEmpty()) {
                    ContentSection(label = "Image URL", value = branchLinkData.imageUrl)
                }

                if (branchLinkData.metadata.isNotEmpty()) {
                    Text(
                        text = "Metadata:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    branchLinkData.metadata.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$key:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No meaningful content found in this deep link",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
private fun ContentSection(label: String, value: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DeepLinkErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error Processing Deep Link",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retry")
                }

                Button(
                    onClick = onNavigateHome,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Go Home")
                }
            }
        }
    }
}
