package com.sena.crud.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sena.crud.data.local.dao.TaskDraftDao
import com.sena.crud.data.mapper.toDomain
import com.sena.crud.data.mapper.toEntity
import com.sena.crud.data.mapper.toMap
import com.sena.crud.domain.model.Task
import com.sena.crud.domain.repository.TaskRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val taskDraftDao: TaskDraftDao
) : TaskRepository {

    override fun getRemoteTasks(ownerId: String): Flow<List<Task>> = callbackFlow {
        val listener = firestore.collection("tasks")
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tasks = snapshot.documents.map { doc ->
                        doc.data.orEmpty().toDomain(doc.id)
                    }.sortedByDescending { it.createdAt }
                    trySend(tasks)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveTaskRemotely(task: Task): Result<Unit> = try {
        if (task.id.isEmpty()) {
            firestore.collection("tasks").add(task.toMap()).await()
        } else {
            firestore.collection("tasks").document(task.id).set(task.toMap()).await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteTaskRemotely(taskId: String): Result<Unit> = try {
        firestore.collection("tasks").document(taskId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getLocalDrafts(ownerId: String): Flow<List<Task>> {
        return taskDraftDao.getDraftsByOwner(ownerId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveLocalDraft(task: Task) {
        val taskWithId = if (task.id.isEmpty()) {
            task.copy(id = UUID.randomUUID().toString())
        } else {
            task
        }
        taskDraftDao.insertDraft(taskWithId.toEntity())
    }

    override suspend fun deleteLocalDraft(taskId: String) {
        taskDraftDao.deleteDraftById(taskId)
    }

    override suspend fun publishTask(task: Task): Result<Unit> {
        val taskWithId = if (task.id.isEmpty()) {
            task.copy(id = UUID.randomUUID().toString())
        } else {
            task
        }
        return try {
            firestore.collection("tasks").document(taskWithId.id).set(taskWithId.toMap()).await()
            taskDraftDao.deleteDraftById(taskWithId.id)
            Result.success(Unit)
        } catch (e: Exception) {
            taskDraftDao.insertDraft(taskWithId.toEntity())
            Result.failure(e)
        }
    }
}
