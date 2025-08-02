package com.example.branchio.features.home.infoModels

import com.example.branchio.domain.entity.BranchLinkData

sealed interface HomeScreenEvent {
    object OnGenerateLinkClicked : HomeScreenEvent
//    object NavigateToDeepLink : HomeScreenEvent
}

sealed interface HomeScreenEffect {
    data class ShowSnackbar(val message: String) : HomeScreenEffect
    data class ShareGeneratedLink(val url: String) : HomeScreenEffect
//    data class NavigateToDeepLink(val data : BranchLinkData) : HomeScreenEffect
}

data class HomeScreenState(
    val isLoading: Boolean = false,
    val generatedLink: String? = null,
    val error: String? = null
)