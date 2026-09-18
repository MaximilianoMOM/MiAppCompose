package com.sena.crud.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    taskViewModel: TaskViewModel,
    ownerId: String,
    taskId: String,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val operationState by taskViewModel.operationState.collectAsState()

    // Cargar datos si estamos editando una tarea existente
    LaunchedEffect(taskId) {
        if (taskId.isNotEmpty()) {
            val currentTasks = (taskViewModel.tasksUiState.value as? TaskListUiState.Success)?.tasks
            val taskToEdit = currentTasks?.find { it.id == taskId }
            if (taskToEdit != null) {
                title = taskToEdit.title
                description = taskToEdit.description
            }
        }
    }

    LaunchedEffect(operationState) {
        if (operationState is OperationState.Success) {
            taskViewModel.resetOperationState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId.isEmpty()) "Nueva Tarea" else "Editar Tarea") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; taskViewModel.resetOperationState() },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(32.dp))

                if (operationState is OperationState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { taskViewModel.saveDraftLocal(title, description, ownerId, taskId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Guardar Borrador")
                        }

                        Button(
                            onClick = { taskViewModel.publishTask(title, description, ownerId, taskId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Publicar en la Nube")
                        }
                    }
                }

                if (operationState is OperationState.Error) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = (operationState as OperationState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
