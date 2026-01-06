package com.deicon.kmp_playground.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deicon.kmp_playground.models.Todo
import com.deicon.kmp_playground.utils.TimeUtils
import com.deicon.kmp_playground.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    projectId: String?,
    onBack: () -> Unit
) {
    val todos by viewModel.todos.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newTodoTitle by remember { mutableStateOf("") }
    var newTodoDescription by remember { mutableStateOf("") }
    var newTodoEstimatedTime by remember { mutableStateOf("") }

    LaunchedEffect(projectId) {
        if (projectId != null) {
            viewModel.loadTodos(projectId)
            viewModel.loadProjectStats(projectId)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }
            Text(
                text = "Todos",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Stats card
        stats?.let { projectStats ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Project Statistics", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estimated: ${TimeUtils.formatTime(projectStats.totalEstimatedHours)}")
                    Text("Actual: ${TimeUtils.formatTime(projectStats.totalActualHours)}")
                    Text("Todos: ${projectStats.completedTodoCount}/${projectStats.todoCount}")
                }
            }
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.padding(bottom = 16.dp),
            enabled = projectId != null
        ) {
            Text("Add Todo")
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(todos) { todo ->
                    TodoCard(
                        todo = todo,
                        onToggleComplete = {
                            scope.launch {
                                viewModel.toggleComplete(todo.id, todo.completed)
                            }
                        },
                        onDelete = {
                            scope.launch {
                                projectId?.let {
                                    viewModel.deleteTodo(todo.id, it)
                                }
                            }
                        }
                    )
                }
            }
        }

        if (showDialog && projectId != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Todo") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTodoTitle,
                            onValueChange = { newTodoTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newTodoDescription,
                            onValueChange = { newTodoDescription = it },
                            label = { Text("Description (optional)") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newTodoEstimatedTime,
                            onValueChange = { newTodoEstimatedTime = it },
                            label = { Text("Estimated Time (e.g., 2h, 1.5d, 1w)") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Format: 2h, 1.5d, or 1w") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val estimatedHours = if (newTodoEstimatedTime.isNotBlank()) {
                                    TimeUtils.parseTime(newTodoEstimatedTime)
                                } else null

                                viewModel.createTodo(
                                    Todo(
                                        id = "",
                                        projectId = projectId,
                                        title = newTodoTitle,
                                        description = newTodoDescription.ifBlank { null },
                                        dueDate = null,
                                        estimatedHours = estimatedHours,
                                        actualHours = 0.0,
                                        completed = false,
                                        createdAt = Clock.System.now(),
                                        completedAt = null
                                    )
                                )
                                newTodoTitle = ""
                                newTodoDescription = ""
                                newTodoEstimatedTime = ""
                                showDialog = false
                            }
                        },
                        enabled = newTodoTitle.isNotBlank()
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TodoCard(
    todo: Todo,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.completed,
                onCheckedChange = { onToggleComplete() }
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium
                )
                todo.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    todo.estimatedHours?.let {
                        Text(
                            text = "Est: ${TimeUtils.formatTime(it)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Actual: ${TimeUtils.formatTime(todo.actualHours)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
