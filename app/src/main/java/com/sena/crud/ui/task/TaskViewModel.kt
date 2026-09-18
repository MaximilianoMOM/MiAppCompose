package com.sena.crud.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.crud.domain.model.Task
import com.sena.crud.domain.usecase.task.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TaskListUiState {
    object Loading : TaskListUiState
    data class Success(val tasks: List<Task>) : TaskListUiState
    data class Error(val message: String) : TaskListUiState
}

sealed interface DraftsUiState {
    data class Success(val drafts: List<Task>) : DraftsUiState
}

sealed interface OperationState {
    object Idle : OperationState
    object Loading : OperationState
    object Success : OperationState
    data class Error(val message: String) : OperationState
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases
) : ViewModel() {

    private val _tasksUiState = MutableStateFlow<TaskListUiState>(TaskListUiState.Loading)
    val tasksUiState: StateFlow<TaskListUiState> = _tasksUiState.asStateFlow()

    private val _draftsUiState = MutableStateFlow<DraftsUiState>(DraftsUiState.Success(emptyList()))
    val draftsUiState: StateFlow<DraftsUiState> = _draftsUiState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private var tasksJob: Job? = null
    private var draftsJob: Job? = null

    fun loadData(ownerId: String) {
        tasksJob?.cancel()
        draftsJob?.cancel()

        tasksJob = viewModelScope.launch {
            taskUseCases.getTasks(ownerId)
                .catch { e -> _tasksUiState.value = TaskListUiState.Error(e.message ?: "Error al obtener tareas") }
                .collect { list -> _tasksUiState.value = TaskListUiState.Success(list) }
        }

        draftsJob = viewModelScope.launch {
            taskUseCases.getDrafts(ownerId)
                .collect { list -> _draftsUiState.value = DraftsUiState.Success(list) }
        }
    }

    fun publishTask(title: String, description: String, ownerId: String, existingId: String = "") {
        if (title.isBlank()) {
            _operationState.value = OperationState.Error("El título no puede estar vacío.")
            return
        }

        _operationState.value = OperationState.Loading
        viewModelScope.launch {
            val task = Task(
                id = existingId,
                title = title,
                description = description,
                ownerId = ownerId
            )
            taskUseCases.publishTask(task).fold(
                onSuccess = { _operationState.value = OperationState.Success },
                onFailure = { error -> _operationState.value = OperationState.Error(error.message ?: "Error al publicar") }
            )
        }
    }

    fun saveDraftLocal(title: String, description: String, ownerId: String, existingId: String = "") {
        if (title.isBlank()) {
            _operationState.value = OperationState.Error("El título no puede estar vacío.")
            return
        }

        viewModelScope.launch {
            val task = Task(
                id = existingId,
                title = title,
                description = description,
                ownerId = ownerId
            )
            taskUseCases.saveDraft(task)
            _operationState.value = OperationState.Success
        }
    }

    fun publishDraftDirectly(task: Task) {
        viewModelScope.launch {
            taskUseCases.publishTask(task)
        }
    }

    fun deleteRemoteTask(taskId: String) {
        viewModelScope.launch {
            taskUseCases.deleteTask(taskId)
        }
    }

    fun deleteLocalDraft(taskId: String) {
        viewModelScope.launch {
            taskUseCases.deleteDraft(taskId)
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
