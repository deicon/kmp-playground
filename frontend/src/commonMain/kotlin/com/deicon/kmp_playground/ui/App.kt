package com.deicon.kmp_playground.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.deicon.kmp_playground.client.TodoApiClientImpl
import com.deicon.kmp_playground.viewmodel.CustomerViewModel
import com.deicon.kmp_playground.viewmodel.ProjectViewModel
import com.deicon.kmp_playground.viewmodel.TodoViewModel

enum class Screen {
    CUSTOMERS, PROJECTS, TODOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val apiClient = remember { TodoApiClientImpl() }
    val customerViewModel = remember { CustomerViewModel(apiClient) }
    val projectViewModel = remember { ProjectViewModel(apiClient) }
    val todoViewModel = remember { TodoViewModel(apiClient) }

    var currentScreen by remember { mutableStateOf(Screen.CUSTOMERS) }
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("KMP Playground - Todo App") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                when (currentScreen) {
                    Screen.CUSTOMERS -> CustomerScreen(
                        viewModel = customerViewModel,
                        onCustomerSelected = { customerId ->
                            selectedCustomerId = customerId
                            currentScreen = Screen.PROJECTS
                        }
                    )
                    Screen.PROJECTS -> ProjectScreen(
                        viewModel = projectViewModel,
                        customerId = selectedCustomerId,
                        onProjectSelected = { projectId ->
                            selectedProjectId = projectId
                            currentScreen = Screen.TODOS
                        },
                        onBack = {
                            selectedCustomerId = null
                            currentScreen = Screen.CUSTOMERS
                        }
                    )
                    Screen.TODOS -> TodoScreen(
                        viewModel = todoViewModel,
                        projectId = selectedProjectId,
                        onBack = {
                            selectedProjectId = null
                            currentScreen = Screen.PROJECTS
                        }
                    )
                }
            }
        }
    }
}
