package com.qhack.application.services.customer

import LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CustomerRequestDto(
    val firstName: String,
    val lastName: String,
    @Serializable(with = LocalDateSerializer::class)
    val birthDate: LocalDate? = null,
)

@Serializable
data class CustomerResponseDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    @Serializable(with = LocalDateSerializer::class)
    val birthDate: LocalDate? = null,
)

@Serializable
data class CustomerWithPropertiesResponseDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    @Serializable(with = LocalDateSerializer::class)
    val birthDate: LocalDate? = null,
    val properties: List<com.qhack.application.services.property.PropertyResponseDto> = emptyList()
)