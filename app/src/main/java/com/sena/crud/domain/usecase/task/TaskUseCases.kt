package com.sena.crud.domain.usecase.task

import com.sena.crud.domain.model.Task
import com.sena.crud.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class TaskUseCases @Inject constructor(
    val getTasks: GetTasksUseCase,
    val publishTask: PublishTaskUseCase,
    val deleteTask: DeleteTaskUseCase,
    val getDrafts: GetDraftsUseCase,
    val saveDraft: SaveDraftUseCase,
    val deleteDraft: DeleteDraftUseCase
)

class GetTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(ownerId: String): Flow<List<Task>> = repository.getRemoteTasks(ownerId)
}

class PublishTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) = repository.publishTask(task)
}

class DeleteTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String) = repository.deleteTaskRemotely(taskId)
}

class GetDraftsUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke(ownerId: String): Flow<List<Task>> = repository.getLocalDrafts(ownerId)
}

class SaveDraftUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) = repository.saveLocalDraft(task)
}

class DeleteDraftUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String) = repository.deleteLocalDraft(taskId)
}
