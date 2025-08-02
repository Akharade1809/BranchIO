package com.example.branchio.domain.usecases

import android.content.Context
import com.example.branchio.domain.entity.BranchLinkData
import com.example.branchio.domain.repository.BranchRepository

class GenerateBranchLinkUseCase(
    private val repository : BranchRepository
) {
    suspend operator fun invoke(data : BranchLinkData): Result<String> {
        return repository.generateBranchLink(data)
    }
}