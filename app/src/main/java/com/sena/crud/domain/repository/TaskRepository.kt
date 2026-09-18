package com.sena.crud.domain.repository

import com.sena.crud.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // Firestore (Remoto)
    fun getRemoteTasks(ownerId: String): Flow<List<Task>>
    suspend fun saveTaskRemotely(task: Task): Result<Unit>
    suspend fun deleteTaskRemotely(taskId: String): Result<Unit>

    // Room (Local - Borradores)
    fun getLocalDrafts(ownerId: String): Flow<List<Task>>
    suspend fun saveLocalDraft(task: Task)
    suspend fun deleteLocalDraft(taskId: String)
    
    // Operación combinada (Publicación segura)
    suspend fun publishTask(task: Task): Result<Unit>
}
