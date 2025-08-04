package com.example.branchio.domain.usecases

import android.app.Activity
import android.content.Intent
import com.example.branchio.domain.entity.BranchLinkData
import com.example.branchio.domain.repository.BranchRepository

class HandleDeepLinkUseCase(
    private val branchRepository: BranchRepository
) {
    suspend operator fun invoke(activity : Activity, intent : Intent) : Result<BranchLinkData> {
        return branchRepository.handleDeepLink(activity ,intent)
    }
}