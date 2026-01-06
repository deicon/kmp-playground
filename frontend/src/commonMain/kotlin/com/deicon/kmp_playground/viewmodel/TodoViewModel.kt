package com.deicon.kmp_playground.viewmodel

import com.deicon.kmp_playground.api.ProjectTimeStats
import com.deicon.kmp_playground.api.TodoApiClient
import com.deicon.kmp_playground.models.Todo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TodoViewModel(private val apiClient: TodoApiClient) {
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private val _stats = MutableStateFlow<ProjectTimeStats?>(null)
    val stats: StateFlow<ProjectTimeStats?> = _stats.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadTodos(projectId: String? = null) {
        _loading.value = true
        _error.value = null
        try {
            _todos.value = if (projectId != null) {
                apiClient.getTodosByProjectId(projectId)
            } else {
                apiClient.getAllTodos()
            }
        } catch (e: Exception) {
            _error.value = "Failed to load todos: ${e.message}"
        } finally {
            _loading.value = false
        }
    }

    suspend fun loadProjectStats(projectId: String) {
        try {
            _stats.value = apiClient.getProjectTimeStats(projectId)
        } catch (e: Exception) {
            _error.value = "Failed to load stats: ${e.message}"
        }
    }

    suspend fun createTodo(todo: Todo) {
        _loading.value = true
        _error.value = null
        try {
            apiClient.createTodo(todo)
            loadTodos(todo.projectId)
        } catch (e: Exception) {
            _error.value = "Failed to create todo: ${e.message}"
        } finally {
            _loading.value = false
        }
    }

    suspend fun toggleComplete(id: String, completed: Boolean) {
        _loading.value = true
        _error.value = null
        try {
            if (completed) {
                apiClient.markTodoIncomplete(id)
            } else {
                apiClient.markTodoCompleted(id)
            }
            // Reload the current list
            val projectId = _todos.value.find { it.id == id }?.projectId
            loadTodos(projectId)
        } catch (e: Exception) {
            _error.value = "Failed to update todo: ${e.message}"
        } finally {
            _loading.value = false
        }
    }

    suspend fun deleteTodo(id: String, projectId: String) {
        _loading.value = true
        _error.value = null
        try {
            apiClient.deleteTodo(id)
            loadTodos(projectId)
        } catch (e: Exception) {
            _error.value = "Failed to delete todo: ${e.message}"
        } finally {
            _loading.value = false
        }
    }
}
