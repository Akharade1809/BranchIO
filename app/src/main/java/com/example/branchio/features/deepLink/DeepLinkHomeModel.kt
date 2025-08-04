package com.example.branchio.features.deepLink

import android.content.Intent
import com.example.branchio.domain.entity.BranchLinkData

sealed class DeepLinkEvent {
    data class HandleDeepLinkIntent(val intent: Intent) : DeepLinkEvent()
    data class RetryDeepLink(val intent: Intent?) : DeepLinkEvent()
    object ClearError : DeepLinkEvent()
}

// Updated Effects
sealed class DeepLinkEffect {
    data class ShowError(val message: String) : DeepLinkEffect()
    data class ShowSuccess(val message: String) : DeepLinkEffect()
    data class ShowInfo(val message: String) : DeepLinkEffect()
    object NavigateToHome : DeepLinkEffect()
}

// States remain the same
sealed class DeepLinkState {
    object Idle : DeepLinkState()
    object Loading : DeepLinkState()
    data class Success(val data: BranchLinkData) : DeepLinkState()
    data class Error(val message: String) : DeepLinkState()
}