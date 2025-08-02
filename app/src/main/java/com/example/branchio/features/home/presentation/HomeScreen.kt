package com.example.branchio.features.presentation.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(it.url)
                    }
                    startActivity(context, intent, null)
                }
//                is HomeScreenEffect.NavigateToDeepLink -> {
//                   val jsonData = Uri.encode(Json.encodeToString(it.data))
//                    navController.navigate(Screen.DeepLink.passData(data = jsonData))
//                }
            }
        }
    }

    HomeContent(
        viewState = viewState,
        onGenerateLinkClick = {
            viewModel.onEvent(HomeScreenEvent.OnGenerateLinkClicked)
        },
//        onNavigateLinkClick = {
//            viewModel.onEvent(HomeScreenEvent.NavigateToDeepLink)
//        }
    )
}