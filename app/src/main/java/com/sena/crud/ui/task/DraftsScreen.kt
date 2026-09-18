package com.sena.crud.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sena.crud.domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftsScreen(
    taskViewModel: TaskViewModel,
    onNavigateBack: () -> Unit
) {
    val draftsState by taskViewModel.draftsUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borradores Locales (Room)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            val list = (draftsState as? DraftsUiState.Success)?.drafts ?: emptyList()
            if (list.isEmpty()) {
                Text(text = "No hay borradores guardados localmente.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(list) { task ->
                        DraftItem(
                            task = task,
                            onPublishClick = { taskViewModel.publishDraftDirectly(task) },
                            onDeleteClick = { taskViewModel.deleteLocalDraft(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DraftItem(
    task: Task,
    onPublishClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Row {
                IconButton(onClick = onPublishClick) {
                    Icon(Icons.Default.Share, contentDescription = "Subir a la nube", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Borrador", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
