package com.example.branchio.domain.usecases

import com.example.branchio.data.models.Product
import com.example.branchio.domain.repository.BranchRepository
import io.branch.indexing.BranchUniversalObject

class TrackCustomEventUseCase(private val branchRepository: BranchRepository) {
    operator fun invoke(
        eventName : String,
        product : Product? = null,
        customData : Map<String, Any> = emptyMap()
    ){
        val buo = product?.let {
            BranchUniversalObject()
                .setCanonicalIdentifier("content/${it.id}")
                .setTitle(it.title)
        }

        branchRepository.trackEvent(eventName,buo, customData)

    }
}