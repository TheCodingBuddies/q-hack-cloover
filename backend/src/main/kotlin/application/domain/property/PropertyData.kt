package com.qhack.application.domain.property

data class PropertyData(
    val postCode: String,
    val street: String,
    val city: String,
    val houseNumber: String,
    val customerId: Int,
    val sunnyScore: Int? = null,
    val metadata: Map<String, String>? = null
)