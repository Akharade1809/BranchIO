package com.example.branchio.core.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.branchio.domain.usecases.InitBranchSessionUseCase
import com.example.branchio.domain.usecases.ReinitBranchSessionUseCase
import io.branch.referral.Branch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class AppEffect {
    data class NavigateToProductDetail(val productId: Int) : AppEffect()
    object NavigateToHome : AppEffect()
    data class ShowError(val message: String) : AppEffect()
}


class AppViewModel(
    private val initBranchSessionUseCase: InitBranchSessionUseCase,
    private val reinitBranchSessionUseCase: ReinitBranchSessionUseCase,
) : ViewModel() {


    private val _navigationEvents = MutableSharedFlow<AppEffect>(
        replay = 1,
        extraBufferCapacity = 3
    )
    val navigationEvents: SharedFlow<AppEffect> = _navigationEvents.asSharedFlow()

    fun initBranch(activity: Activity, data: Uri?) {
        Log.d("AppViewModel", " initBranch called with data: $data")
        initBranchSessionUseCase(activity, data) { buo, linkProps, error ->
            if (error != null) {
                Log.e("AppViewModel", " initBranch error: ${error.message}")

            } else {
                Log.i("AppViewModel", " Branch session initialized successfully")
                Log.i("BranchInit", "Data: ${buo?.title}, linkProps: ${linkProps?.channel}")

                Log.d("AppViewModel", " Processing deep link after initialization...")
                //  Process deep link after successful initialization
                viewModelScope.launch {
//                    delay(1000) // Wait for UI to be ready
                    processLatestReferringParams()
                }
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

                    // Process deep link after successful re-initialization
                    viewModelScope.launch {
//                        delay(1000) // Wait for UI to be ready
                        processLatestReferringParams()
                    }
                }
            }
        }
    }

    private fun processLatestReferringParams() {
        try {
            val referringParams = Branch.getInstance().latestReferringParams
            Log.d("AppViewModel", " Processing latest referring params: $referringParams")

            if (referringParams != null && referringParams.length() > 0) {
                val isClickedBranchLink = referringParams.optBoolean("+clicked_branch_link", false)

                if (isClickedBranchLink) {
                    val productId = referringParams.optString("product_id", "")
                    val pageType = referringParams.optString("page_type", "")

                    Log.d("AppViewModel", " Deep link data - Product ID: $productId, Page Type: $pageType")

                    if (pageType == "product_detail" && productId.isNotEmpty()) {
                        val productIdInt = productId.toIntOrNull()
                        if (productIdInt != null) {
                            Log.d("AppViewModel", " EMITTING NAVIGATION EVENT: Product Detail $productIdInt")

                            viewModelScope.launch {
                                val success = _navigationEvents.tryEmit(AppEffect.NavigateToProductDetail(productIdInt))
                                Log.d("AppViewModel", "📤 Navigation event emission success: $success")

                                if (!success) {
                                    Log.e("AppViewModel", " Failed to emit navigation event - buffer full?")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AppViewModel", " Error processing latest referring params", e)
        }
    }
}





