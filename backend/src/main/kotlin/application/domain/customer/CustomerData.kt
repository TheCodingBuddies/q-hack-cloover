package com.qhack.application.domain.customer

import java.time.LocalDate

data class CustomerData(
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate? = null,
)