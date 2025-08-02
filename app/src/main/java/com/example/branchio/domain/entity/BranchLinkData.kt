package com.example.branchio.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class BranchLinkData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val metadata: Map<String, String>
)
