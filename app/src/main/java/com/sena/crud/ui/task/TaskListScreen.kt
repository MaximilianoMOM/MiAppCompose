package com.sena.crud.ui.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sena.crud.domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    taskViewModel: TaskViewModel,
    ownerId: String,
    ownerName: String,
    onNavigateToForm: (String) -> Unit,
    onNavigateToDrafts: () -> Unit,
    onLogout: () -> Unit
) {
    val tasksUiState by taskViewModel.tasksUiState.collectAsState()

    LaunchedEffect(ownerId) {
        taskViewModel.loadData(ownerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas de $ownerName") },
                actions = {
                    IconButton(onClick = onNavigateToDrafts) {
                        Icon(Icons.Default.List, contentDescription = "Ver Borradores")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm("") }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Tarea")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = tasksUiState) {
                is TaskListUiState.Loading -> CircularProgressIndicator()
                is TaskListUiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error)
                is TaskListUiState.Success -> {
                    if (state.tasks.isEmpty()) {
                        Text(text = "No tienes tareas registradas.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.tasks) { task ->
                                TaskItem(
                                    task = task,
                                    onItemClick = { onNavigateToForm(task.id) },
                                    onDeleteClick = { taskViewModel.deleteRemoteTask(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar Tarea", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
