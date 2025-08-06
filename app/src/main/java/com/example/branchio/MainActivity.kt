package com.example.branchio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.branchio.core.navigation.AppNavGraph
import com.example.branchio.core.theme.BranchIOTheme
import com.example.branchio.core.viewmodel.AppViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val appViewModel : AppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("MainActivity", "🚀 onCreate called")
        setContent {
            BranchIOTheme {
                AppNavGraph(appViewModel = appViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "▶️ onStart called")
        Log.d("MainActivity", "🔗 Intent data: ${intent?.data}")
        Log.d("MainActivity", "📦 Intent extras: ${intent?.extras}")

        if (intent?.data != null) {
            intent.putExtra("branch_force_new_session", true)
            Log.d("MainActivity", "🔄 Force new session set for deep link")
        }

        appViewModel.initBranch(this, intent?.data)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "🆕 onNewIntent called")
        Log.d("MainActivity", "🔗 New intent data: ${intent.data}")

        setIntent(intent)
        intent.putExtra("branch_force_new_session", true)
        appViewModel.reInitBranch(this, intent)
    }

}
