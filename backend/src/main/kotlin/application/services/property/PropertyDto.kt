package com.qhack.application.services.property

import kotlinx.serialization.Serializable

@Serializable
data class PropertyRequestDto(
    val postCode: String,
    val street: String,
    val city: String,
    val houseNumber: String
)
