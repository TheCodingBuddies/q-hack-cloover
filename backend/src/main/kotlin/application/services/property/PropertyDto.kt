package com.qhack.application.services.property

import kotlinx.serialization.Serializable

@Serializable
data class PropertyRequestDto(
    val postCode: String,
    val street: String,
    val city: String,
    val houseNumber: String
)

@Serializable
data class PropertyResponseDto(
    val id: Int,
    val sunnyScore: SunnyScoreResponse? = null
)

@Serializable
data class SunnyScoreResponse(
    val address: String,
    val sunnyPlace: Int,
    val explanation: String
)
