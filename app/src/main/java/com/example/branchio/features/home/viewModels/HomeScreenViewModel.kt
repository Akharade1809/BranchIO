package com.example.branchio.features.home.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.branchio.domain.entity.BranchLinkData
import com.example.branchio.domain.usecases.GenerateBranchLinkUseCase
import com.example.branchio.features.home.infoModels.HomeScreenEffect
import com.example.branchio.features.home.infoModels.HomeScreenEvent
import com.example.branchio.features.home.infoModels.HomeScreenState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val generateBranchLinkUseCase: GenerateBranchLinkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeScreenEffect>()
    val effect: SharedFlow<HomeScreenEffect> = _effect.asSharedFlow()

    val data = BranchLinkData(
        title = "Check out this cool content!",
        description = "This is a deep link to specific content.",
        imageUrl = "https://picsum.photos/id/237/200/300",
        metadata = mapOf(
            "item_id" to "12345",
            "type" to "lorem_picsum"
        )
    )

    fun onEvent(event: HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnGenerateLinkClicked -> generateLink()
            is HomeScreenEvent.OnNavigateToDeepLinkClicked -> navigateToDeepLink()
        }
    }

    private fun navigateToDeepLink() {
        viewModelScope.launch {
            val currentLink = _state.value.generatedLink
            if (currentLink != null) {
                _effect.emit(HomeScreenEffect.NavigateToDeepLink(currentLink))
            } else {
                _effect.emit(HomeScreenEffect.ShowSnackbar("Please generate a link first"))
            }
        }
    }

    private fun generateLink() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val result = generateBranchLinkUseCase(data)

                result.fold(
                    onSuccess = { url ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                generatedLink = url,
                                canNavigate = true // Enable navigation button
                            )
                        }
                        _effect.emit(HomeScreenEffect.ShareGeneratedLink(url))
                        _effect.emit(HomeScreenEffect.ShowSnackbar("Link generated successfully!"))
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = error.message,
                                canNavigate = false
                            )
                        }
                        _effect.emit(HomeScreenEffect.ShowSnackbar("Failed to generate link: ${error.message}"))
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                        canNavigate = false
                    )
                }
                _effect.emit(HomeScreenEffect.ShowSnackbar("Unexpected error: ${e.message}"))
            }
        }
    }
}
