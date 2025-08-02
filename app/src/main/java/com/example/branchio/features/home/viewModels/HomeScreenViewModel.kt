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


    fun onEvent(event : HomeScreenEvent) {
        when (event) {
            is HomeScreenEvent.OnGenerateLinkClicked -> generateLink()
//            HomeScreenEvent.NavigateToDeepLink -> navigateToDeepLink()
        }
    }

//    private fun navigateToDeepLink(){
//        viewModelScope.launch {
//            val data = BranchLinkData(
//                title = "Demo Title",
//                description = "Demo Description",
//                imageUrl = "https://placehold.co/600x400",
//                metadata = mapOf("author" to "Arjun", "version" to "1.0")
//            )
//            _effect.emit(HomeScreenEffect.NavigateToDeepLink(data))
//        }
//
//    }

    val data = BranchLinkData(
        title = "Check out this cool content!",
        description = "This is a deep link to specific content.",
        imageUrl = "https://example.com/image.jpg",
        metadata = mapOf("item_id" to "12345", "type" to "example")
    )

    private fun generateLink() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = generateBranchLinkUseCase(data)

            result.fold(
                onSuccess = { url ->
                    _state.update { it.copy(isLoading = false, generatedLink = url) }
                    _effect.emit(HomeScreenEffect.ShareGeneratedLink(url))
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                    _effect.emit(HomeScreenEffect.ShowSnackbar("Failed to generate link"))
                }
            )
        }
    }
}