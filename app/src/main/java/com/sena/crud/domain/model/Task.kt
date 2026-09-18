package com.sena.crud.domain.model

data class Task(
    val id: String = "",
    val title: String,
    val description: String,
    val ownerId: String,
    val createdAt: Long = System.currentTimeMillis()
)
