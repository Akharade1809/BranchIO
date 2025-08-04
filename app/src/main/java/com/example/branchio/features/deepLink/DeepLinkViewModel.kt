package com.example.branchio.features.deepLink

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.branchio.domain.entity.BranchLinkData
import com.example.branchio.domain.usecases.HandleDeepLinkUseCase
import com.example.branchio.util.BranchLinkExtractor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeepLinkViewModel(
    private var handleDeepLinkUseCase: HandleDeepLinkUseCase
) : ViewModel(){
    private val _state = MutableStateFlow<DeepLinkState>(DeepLinkState.Idle)
    val state : StateFlow<DeepLinkState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DeepLinkEffect>()
    val effect : SharedFlow<DeepLinkEffect> = _effect.asSharedFlow()

    fun onEvent(event: DeepLinkEvent){
        when (event) {
            is DeepLinkEvent.HandleDeepLinkIntent -> {
                handleDeepLinkEvent(event.intent)
            }
            is DeepLinkEvent.RetryDeepLink -> {
                event.intent?.let { handleDeepLinkEvent(it) }
            }
            DeepLinkEvent.ClearError -> {
                if (_state.value is DeepLinkState.Error) {
                    _state.value = DeepLinkState.Idle
                }
            }
        }

    }

    private fun handleDeepLinkEvent(intent : Intent){
        viewModelScope.launch {

            try{
                _state.value = DeepLinkState.Loading


                Log.d("DeepLinkViewModel", "Processing intent: $intent")
                Log.d("DeepLinkViewModel", "Intent data: ${intent.data}")
                Log.d("DeepLinkViewModel", "Intent extras: ${intent.extras}")

                val uri = intent.data
                if (uri == null) {
                    val fallbackPath = intent.getStringExtra("deeplink")
                    Log.w("DeepLinkViewModel", "No URI in intent, fallback path: $fallbackPath")

                    if (fallbackPath.isNullOrEmpty()) {
                        _state.value = DeepLinkState.Error("No deep link data found")
                        _effect.emit(DeepLinkEffect.ShowError("No deep link data found"))
                        return@launch
                    }

                    intent.data = Uri.parse("https://n2ujk.test-app.link/$fallbackPath")
                }
                val activity = getCurrentActivity() ?: run {
                    _state.value = DeepLinkState.Error("Activity not available")
                    _effect.emit(DeepLinkEffect.ShowError("Activity not available"))
                    return@launch
                }

                val result = handleDeepLinkUseCase(activity ,intent)

                Log.d("DeepLinkViewModel", "handleDeepLinkEvent: ")

                result.fold(
                    onSuccess = { data ->
                        Log.d("BranchDeepLink", "Metadata: ${data.metadata}")

                        Log.d("DeepLinkViewModel", "Deep link processing successful")
                        Log.d("DeepLinkViewModel", "Title: ${data.title}")
                        Log.d("DeepLinkViewModel", "Description: ${data.description}")
                        Log.d("DeepLinkViewModel", "ImageUrl: ${data.imageUrl}")
                        Log.d("DeepLinkViewModel", "Metadata: ${data.metadata}")
                        _state.value = DeepLinkState.Success(data)

                        if (BranchLinkExtractor.hasValidContent(data)) {

                            _effect.emit(DeepLinkEffect.ShowSuccess("Deep link processed successfully"))
                        } else {
                            Log.w("DeepLinkViewModel", "No meaningful content in deep link")
                            _state.value = DeepLinkState.Success(data)
                            _effect.emit(DeepLinkEffect.ShowInfo("Deep link processed but contains no content"))
                        }
                    },
                    onFailure = { error ->
                        Log.e("DeepLinkViewModel", "Deep link processing failed", error)
                        val errorMessage = error.message ?: "Unknown error occurred"
                        _state.value = DeepLinkState.Error(errorMessage)
                        _effect.emit(DeepLinkEffect.ShowError(errorMessage))
                    }
                )
            }catch (e : Exception){
                e.printStackTrace()
            }




        }
    }

    // Store activity reference
    private var currentActivity: Activity? = null

    fun setActivity(activity: Activity) {
        currentActivity = activity
    }

    private fun getCurrentActivity(): Activity? = currentActivity
}