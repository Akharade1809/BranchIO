package com.example.branchio.core.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.branchio.domain.usecases.InitBranchSessionUseCase
import com.example.branchio.domain.usecases.ReinitBranchSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppViewModel(
    private val initBranchSessionUseCase: InitBranchSessionUseCase,
    private val reinitBranchSessionUseCase: ReinitBranchSessionUseCase,
) : ViewModel() {

    private val _deepLinkPath = MutableStateFlow<String?>(null)
    val deepLinkPath : StateFlow<String?> = _deepLinkPath

    fun initBranch(activity: Activity, data: Uri?) {
        initBranchSessionUseCase(activity, data) { buo, linkProps, error ->
            if (error != null) {
                Log.e("BranchInit", "initBranch: error -> ${error.message} ")
            } else {
                Log.i("BranchInit", "Session Started")
                Log.i("BranchInit", "Data: ${buo?.title}, linkProps: ${linkProps?.channel}")
            }
        }
    }

    fun reInitBranch(activity: Activity, intent: Intent?) {
        if (intent?.getBooleanExtra("branch_force_new_session", false) == true) {
            reinitBranchSessionUseCase(activity) { json, error ->
                if (error != null) {
                    Log.e("BranchReInit", error.message ?: "Unknown error")
                } else {
                    Log.i("BranchReInit", json.toString())
                }
            }
        }
    }

}