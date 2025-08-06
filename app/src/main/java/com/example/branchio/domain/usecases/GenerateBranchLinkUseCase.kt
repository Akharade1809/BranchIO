package com.example.branchio.domain.usecases

import com.example.branchio.data.models.Product
import com.example.branchio.domain.repository.BranchRepository

class GenerateBranchLinkUseCase(
    private val repository : BranchRepository
) {
    suspend operator fun invoke(data : Product): Result<String> {
        return repository.generateBranchLink(data)
    }
}