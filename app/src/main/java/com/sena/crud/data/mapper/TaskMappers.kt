package com.sena.crud.data.mapper

import com.sena.crud.data.local.entity.TaskDraftEntity
import com.sena.crud.domain.model.Task

fun TaskDraftEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    ownerId = ownerId,
    createdAt = createdAt
)

fun Task.toEntity(): TaskDraftEntity = TaskDraftEntity(
    id = id,
    title = title,
    description = description,
    ownerId = ownerId,
    createdAt = createdAt
)

fun Task.toMap(): Map<String, Any> = mapOf(
    "title" to title,
    "description" to description,
    "ownerId" to ownerId,
    "createdAt" to createdAt
)

fun Map<String, Any>.toDomain(documentId: String): Task = Task(
    id = documentId,
    title = (this["title"] as? String).orEmpty(),
    description = (this["description"] as? String).orEmpty(),
    ownerId = (this["ownerId"] as? String).orEmpty(),
    createdAt = (this["createdAt"] as? Long) ?: 0L
)
