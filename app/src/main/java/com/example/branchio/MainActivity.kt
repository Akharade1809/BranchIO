package com.example.branchio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import com.example.branchio.core.navigation.AppNavGraph
import com.example.branchio.core.theme.BranchIOTheme
import com.example.branchio.core.viewmodel.AppViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val appViewModel : AppViewModel by viewModel()

    override fun onStart() {
        super.onStart()
        appViewModel.initBranch(this, intent?.data)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        appViewModel.reInitBranch(this, intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BranchIOTheme {
                AppNavGraph()
            }
        }
    }
}
