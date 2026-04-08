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
data class PropertyDto(
    val postCode: String,
    val street: String,
    val city: String,
    val houseNumber: String,
    val sunnyScore: Int? = null,
)

@Serializable
data class CustomerWithPropertyResponseDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    @Serializable(with = LocalDateSerializer::class)
    val birthDate: LocalDate? = null,
    val property: PropertyDto? = null,
)
