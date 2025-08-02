package com.example.branchio.features.deepLink

import android.content.Intent
import com.example.branchio.domain.entity.BranchLinkData

sealed interface DeepLinkEffect {
    data class ShowError(val message :String) : DeepLinkEffect
    object NaviagteToHome : DeepLinkEffect
}

sealed interface DeepLinkEvent {
    data class HandleDeepLinkIntent(val intent: Intent) : DeepLinkEvent

}

sealed class DeepLinkState{
    object Idle : DeepLinkState()
    object Loading : DeepLinkState()
    data class Success(val data: BranchLinkData) : DeepLinkState()
    data class Error(val message: String) : DeepLinkState()
}