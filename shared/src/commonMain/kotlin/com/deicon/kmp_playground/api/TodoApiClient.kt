package com.deicon.kmp_playground.api

import com.deicon.kmp_playground.models.Customer
import com.deicon.kmp_playground.models.Project
import com.deicon.kmp_playground.models.Todo

/**
 * API client interface for interacting with the backend.
 * This interface will be implemented by the backend using Ktor server
 * and by the frontend using Ktor client.
 */
interface TodoApiClient {

    // Customer endpoints
    suspend fun getAllCustomers(): List<Customer>
    suspend fun getCustomerById(id: String): Customer?
    suspend fun createCustomer(customer: Customer): Customer
    suspend fun updateCustomer(id: String, customer: Customer): Customer
    suspend fun deleteCustomer(id: String): Boolean

    // Project endpoints
    suspend fun getAllProjects(): List<Project>
    suspend fun getProjectById(id: String): Project?
    suspend fun getProjectsByCustomerId(customerId: String): List<Project>
    suspend fun createProject(project: Project): Project
    suspend fun updateProject(id: String, project: Project): Project
    suspend fun deleteProject(id: String): Boolean

    // Todo endpoints
    suspend fun getAllTodos(): List<Todo>
    suspend fun getTodoById(id: String): Todo?
    suspend fun getTodosByProjectId(projectId: String): List<Todo>
    suspend fun getCompletedTodos(): List<Todo>
    suspend fun getIncompleteTodos(): List<Todo>
    suspend fun createTodo(todo: Todo): Todo
    suspend fun updateTodo(id: String, todo: Todo): Todo
    suspend fun updateTodoActualHours(id: String, actualHours: Double): Todo
    suspend fun markTodoCompleted(id: String): Todo
    suspend fun markTodoIncomplete(id: String): Todo
    suspend fun deleteTodo(id: String): Boolean

    // Aggregation endpoints
    suspend fun getProjectTimeStats(projectId: String): ProjectTimeStats
}

/**
 * Data class for project time statistics
 */
data class ProjectTimeStats(
    val projectId: String,
    val totalEstimatedHours: Double,
    val totalActualHours: Double,
    val todoCount: Int,
    val completedTodoCount: Int
)
