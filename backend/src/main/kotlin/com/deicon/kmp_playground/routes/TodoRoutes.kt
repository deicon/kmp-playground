package com.deicon.kmp_playground.routes

import com.deicon.kmp_playground.models.CreateTodoRequest
import com.deicon.kmp_playground.models.Todo
import com.deicon.kmp_playground.models.UpdateTodoRequest
import com.deicon.kmp_playground.repository.TodoRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UpdateActualHoursRequest(val actualHours: Double)

fun Route.todoRoutes(repository: TodoRepository) {
    route("/api/todos") {

        get {
            val projectId = call.request.queryParameters["projectId"]
            val completed = call.request.queryParameters["completed"]?.toBoolean()

            val todos = when {
                projectId != null -> repository.getByProjectId(projectId)
                completed == true -> repository.getCompleted()
                completed == false -> repository.getIncomplete()
                else -> repository.getAll()
            }
            call.respond(todos)
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing todo ID")
            )

            val todo = repository.getById(id)
            if (todo != null) {
                call.respond(todo)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo not found"))
            }
        }

        post {
            val request = call.receive<CreateTodoRequest>()
            val newTodo = Todo(
                id = UUID.randomUUID().toString(),
                projectId = request.projectId,
                title = request.title,
                description = request.description,
                estimatedHours = request.estimatedHours,
                actualHours = 0.0,
                completed = false,
                createdAt = Clock.System.now()
            )
            val created = repository.create(newTodo)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing todo ID")
            )

            val request = call.receive<UpdateTodoRequest>()
            val existing = repository.getById(id)
            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo not found"))
                return@put
            }

            val updatedTodo = existing.copy(
                title = request.title,
                description = request.description,
                estimatedHours = request.estimatedHours
            )
            try {
                val updated = repository.update(id, updatedTodo)
                call.respond(updated)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }

        patch("/{id}/actual-hours") {
            val id = call.parameters["id"] ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing todo ID")
            )

            val request = call.receive<UpdateActualHoursRequest>()
            try {
                val updated = repository.updateActualHours(id, request.actualHours)
                call.respond(updated)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }

        post("/{id}/complete") {
            val id = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing todo ID")
            )

            try {
                val updated = repository.markCompleted(id)
                call.respond(updated)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }

        post("/{id}/incomplete") {
            val id = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing todo ID")
            )

            try {
                val updated = repository.markIncomplete(id)
                call.respond(updated)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing todo ID")
            )

            val deleted = repository.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo not found"))
            }
        }

        // Time statistics endpoint
        get("/projects/{projectId}/stats") {
            val projectId = call.parameters["projectId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing project ID")
            )

            val stats = repository.getProjectTimeStats(projectId)
            call.respond(stats)
        }
    }
}
