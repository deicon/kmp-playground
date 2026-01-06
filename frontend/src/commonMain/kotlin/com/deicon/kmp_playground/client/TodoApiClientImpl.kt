package com.deicon.kmp_playground.client

import com.deicon.kmp_playground.api.ProjectTimeStats
import com.deicon.kmp_playground.api.TodoApiClient
import com.deicon.kmp_playground.models.Customer
import com.deicon.kmp_playground.models.Project
import com.deicon.kmp_playground.models.Todo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class TodoApiClientImpl(
    private val baseUrl: String = "http://localhost:8080"
) : TodoApiClient {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    // Customer endpoints
    override suspend fun getAllCustomers(): List<Customer> {
        return client.get("$baseUrl/api/customers").body()
    }

    override suspend fun getCustomerById(id: String): Customer? {
        return try {
            client.get("$baseUrl/api/customers/$id").body()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createCustomer(customer: Customer): Customer {
        return client.post("$baseUrl/api/customers") {
            contentType(ContentType.Application.Json)
            setBody(customer)
        }.body()
    }

    override suspend fun updateCustomer(id: String, customer: Customer): Customer {
        return client.put("$baseUrl/api/customers/$id") {
            contentType(ContentType.Application.Json)
            setBody(customer)
        }.body()
    }

    override suspend fun deleteCustomer(id: String): Boolean {
        return try {
            client.delete("$baseUrl/api/customers/$id")
            true
        } catch (e: Exception) {
            false
        }
    }

    // Project endpoints
    override suspend fun getAllProjects(): List<Project> {
        return client.get("$baseUrl/api/projects").body()
    }

    override suspend fun getProjectById(id: String): Project? {
        return try {
            client.get("$baseUrl/api/projects/$id").body()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getProjectsByCustomerId(customerId: String): List<Project> {
        return client.get("$baseUrl/api/projects") {
            parameter("customerId", customerId)
        }.body()
    }

    override suspend fun createProject(project: Project): Project {
        return client.post("$baseUrl/api/projects") {
            contentType(ContentType.Application.Json)
            setBody(project)
        }.body()
    }

    override suspend fun updateProject(id: String, project: Project): Project {
        return client.put("$baseUrl/api/projects/$id") {
            contentType(ContentType.Application.Json)
            setBody(project)
        }.body()
    }

    override suspend fun deleteProject(id: String): Boolean {
        return try {
            client.delete("$baseUrl/api/projects/$id")
            true
        } catch (e: Exception) {
            false
        }
    }

    // Todo endpoints
    override suspend fun getAllTodos(): List<Todo> {
        return client.get("$baseUrl/api/todos").body()
    }

    override suspend fun getTodoById(id: String): Todo? {
        return try {
            client.get("$baseUrl/api/todos/$id").body()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTodosByProjectId(projectId: String): List<Todo> {
        return client.get("$baseUrl/api/todos") {
            parameter("projectId", projectId)
        }.body()
    }

    override suspend fun getCompletedTodos(): List<Todo> {
        return client.get("$baseUrl/api/todos") {
            parameter("completed", "true")
        }.body()
    }

    override suspend fun getIncompleteTodos(): List<Todo> {
        return client.get("$baseUrl/api/todos") {
            parameter("completed", "false")
        }.body()
    }

    override suspend fun createTodo(todo: Todo): Todo {
        return client.post("$baseUrl/api/todos") {
            contentType(ContentType.Application.Json)
            setBody(todo)
        }.body()
    }

    override suspend fun updateTodo(id: String, todo: Todo): Todo {
        return client.put("$baseUrl/api/todos/$id") {
            contentType(ContentType.Application.Json)
            setBody(todo)
        }.body()
    }

    override suspend fun updateTodoActualHours(id: String, actualHours: Double): Todo {
        return client.patch("$baseUrl/api/todos/$id/actual-hours") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("actualHours" to actualHours))
        }.body()
    }

    override suspend fun markTodoCompleted(id: String): Todo {
        return client.post("$baseUrl/api/todos/$id/complete").body()
    }

    override suspend fun markTodoIncomplete(id: String): Todo {
        return client.post("$baseUrl/api/todos/$id/incomplete").body()
    }

    override suspend fun deleteTodo(id: String): Boolean {
        return try {
            client.delete("$baseUrl/api/todos/$id")
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getProjectTimeStats(projectId: String): ProjectTimeStats {
        return client.get("$baseUrl/api/todos/projects/$projectId/stats").body()
    }
}
