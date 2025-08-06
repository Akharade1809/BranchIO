package com.example.branchio.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Rating(
    val rate : Double,
    val count : Int,
)
