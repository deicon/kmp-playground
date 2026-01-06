package com.deicon.kmp_playground.viewmodel

import com.deicon.kmp_playground.api.TodoApiClient
import com.deicon.kmp_playground.models.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProjectViewModel(private val apiClient: TodoApiClient) {
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadProjects(customerId: String? = null) {
        _loading.value = true
        _error.value = null
        try {
            _projects.value = if (customerId != null) {
                apiClient.getProjectsByCustomerId(customerId)
            } else {
                apiClient.getAllProjects()
            }
        } catch (e: Exception) {
            _error.value = "Failed to load projects: ${e.message}"
        } finally {
            _loading.value = false
        }
    }

    suspend fun createProject(project: Project) {
        _loading.value = true
        _error.value = null
        try {
            apiClient.createProject(project)
            loadProjects()
        } catch (e: Exception) {
            _error.value = "Failed to create project: ${e.message}"
        } finally {
            _loading.value = false
        }
    }

    suspend fun deleteProject(id: String) {
        _loading.value = true
        _error.value = null
        try {
            apiClient.deleteProject(id)
            loadProjects()
        } catch (e: Exception) {
            _error.value = "Failed to delete project: ${e.message}"
        } finally {
            _loading.value = false
        }
    }
}
