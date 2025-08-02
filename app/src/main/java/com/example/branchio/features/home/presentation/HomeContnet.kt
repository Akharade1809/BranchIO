package com.example.branchio.features.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.branchio.features.home.infoModels.HomeScreenState


@Composable
fun HomeContent(
    viewState: HomeScreenState,
    onGenerateLinkClick: () -> Unit,
//    onNavigateLinkClick : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Branch.io Integration", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onGenerateLinkClick) {
            Text("Generate Link")
        }

        Spacer(modifier = Modifier.height(24.dp))

        viewState.generatedLink?.let { url ->
            Text(
                text = "Generated Link:\n$url",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

//        Button(onClick = onNavigateLinkClick) {
//            Text("Navigate to Generated Branch Link")
//        }

        if (viewState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
