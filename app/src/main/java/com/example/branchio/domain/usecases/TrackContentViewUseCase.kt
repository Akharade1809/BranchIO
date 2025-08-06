package com.example.branchio.domain.usecases

import com.example.branchio.data.models.Product
import com.example.branchio.domain.repository.BranchRepository
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.ContentMetadata

class TrackContentViewUseCase(
    private val branchRepository: BranchRepository
) {
    operator fun invoke(product : Product) {
        val buo = BranchUniversalObject()
            .setCanonicalIdentifier("content/${product.id}")
            .setTitle(product.title)
            .setContentDescription(product.description)
            .setContentImageUrl(product.image)
            .setContentMetadata(
                ContentMetadata().apply {
                    addCustomMetadata("product_id", product.id.toString())
                    addCustomMetadata("category", product.category)
                    addCustomMetadata("price", product.price.toString())
                    addCustomMetadata("rating", product.rating.rate.toString())
                }
            )

        branchRepository.trackContentView(buo)
    }
}