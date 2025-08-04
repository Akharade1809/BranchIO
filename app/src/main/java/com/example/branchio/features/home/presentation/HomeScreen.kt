package com.example.branchio.features.presentation.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.branchio.core.navigation.Screen
import com.example.branchio.features.home.infoModels.HomeScreenEffect
import com.example.branchio.features.home.infoModels.HomeScreenEvent
import com.example.branchio.features.home.presentation.HomeContent
import com.example.branchio.features.home.viewModels.HomeScreenViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(null)

    // Handle effects
    LaunchedEffect(effect) {
        effect?.let {
            when (it) {
                is HomeScreenEffect.ShowSnackbar -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
                is HomeScreenEffect.ShareGeneratedLink -> {
                    // Optional: You can remove this if you don't want external sharing
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check this out: ${it.url}")
                    }
                    startActivity(context, Intent.createChooser(intent, "Share Link"), null)
                }
                is HomeScreenEffect.NavigateToDeepLink -> {
                    // Extract the deep link path from the URL
                    val uri = Uri.parse(it.url)
                    val deepLinkPath = when {
                        uri.lastPathSegment != null -> uri.lastPathSegment!!
                        uri.path?.isNotEmpty() == true -> uri.path!!.removePrefix("/")
                        else -> {
                            // Fallback: extract from the full URL
                            val urlParts = it.url.split("/")
                            urlParts.lastOrNull() ?: ""
                        }
                    }

                    Log.d("HomeScreen", "Extracted path: '$deepLinkPath'")

                    if (deepLinkPath.isNotEmpty()) {
                        navController.navigate(Screen.DeepLink.createRoute(deepLinkPath))
                    } else {
                        Toast.makeText(context, "Invalid deep link path", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    HomeContent(
        viewState = viewState,
        onGenerateLinkClick = {
            viewModel.onEvent(HomeScreenEvent.OnGenerateLinkClicked)
        },
        onNavigateToDeepLinkClick = {
            viewModel.onEvent(HomeScreenEvent.OnNavigateToDeepLinkClicked)
        }
    )
}
