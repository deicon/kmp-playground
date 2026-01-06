package com.deicon.kmp_playground.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deicon.kmp_playground.models.Project
import com.deicon.kmp_playground.viewmodel.ProjectViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun ProjectScreen(
    viewModel: ProjectViewModel,
    customerId: String?,
    onProjectSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var newProjectDescription by remember { mutableStateOf("") }

    LaunchedEffect(customerId) {
        viewModel.loadProjects(customerId)
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
                text = "Projects",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.padding(bottom = 16.dp),
            enabled = customerId != null
        ) {
            Text("Add Project")
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
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onProjectSelected(project.id) },
                        onDelete = {
                            scope.launch {
                                viewModel.deleteProject(project.id)
                            }
                        }
                    )
                }
            }
        }

        if (showDialog && customerId != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Project") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newProjectName,
                            onValueChange = { newProjectName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newProjectDescription,
                            onValueChange = { newProjectDescription = it },
                            label = { Text("Description (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.createProject(
                                    Project(
                                        id = "",
                                        customerId = customerId,
                                        name = newProjectName,
                                        description = newProjectDescription.ifBlank { null },
                                        createdAt = Clock.System.now()
                                    )
                                )
                                newProjectName = ""
                                newProjectDescription = ""
                                showDialog = false
                            }
                        }
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
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium
                )
                project.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
