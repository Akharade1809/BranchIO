package com.example.branchio.features.home.infoModels


sealed interface HomeScreenEvent {
    object OnGenerateLinkClicked : HomeScreenEvent
    object OnNavigateToDeepLinkClicked : HomeScreenEvent
}

sealed interface HomeScreenEffect {
    data class ShowSnackbar(val message: String) : HomeScreenEffect
    data class ShareGeneratedLink(val url: String) : HomeScreenEffect
    data class NavigateToDeepLink(val url: String) : HomeScreenEffect
}

data class HomeScreenState(
    val isLoading: Boolean = false,
    val generatedLink: String? = null,
    val error: String? = null,
    val canNavigate: Boolean = false
)
