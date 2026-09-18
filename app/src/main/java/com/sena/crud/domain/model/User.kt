package com.sena.crud.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String? = null
)
