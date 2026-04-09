package com.qhack.application.services.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PropertyRequestDto(
    val postCode: String,
    val street: String,
    val city: String,
    val houseNumber: String,
    val metadata: Map<String, String>? = null
)

@Serializable
data class PropertyResponseDto(
    val id: Int,
    val postCode: String? = null,
    val street: String? = null,
    val city: String? = null,
    val houseNumber: String? = null,
    val sunnyScore: SunnyScoreResponse? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class SunnyScoreResponse(
    val address: String,
    @SerialName("sunnyplace") val sunnyPlace: Int,
    val explanation: String
)
