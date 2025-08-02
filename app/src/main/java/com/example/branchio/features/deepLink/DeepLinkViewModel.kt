package com.example.branchio.features.deepLink

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.branchio.domain.usecases.HandleDeepLinkUseCase
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
        }

    }

    private fun handleDeepLinkEvent(intent : Intent){
        viewModelScope.launch {
            _state.value = DeepLinkState.Loading
            val result = handleDeepLinkUseCase(intent)

            result.fold(
                onSuccess = { data ->
                    _state.value = DeepLinkState.Success(data)
                },
                onFailure = { error ->
                    _state.value = DeepLinkState.Error(message =  error.message.toString())
                    _effect.emit(DeepLinkEffect.ShowError(message = error.message.toString()))
                }
            )
        }
    }
}