package com.deicon.kmp_playground.routes

import com.deicon.kmp_playground.models.Project
import com.deicon.kmp_playground.repository.ProjectRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import java.util.UUID

fun Route.projectRoutes(repository: ProjectRepository) {
    route("/api/projects") {

        get {
            val customerId = call.request.queryParameters["customerId"]
            val projects = if (customerId != null) {
                repository.getByCustomerId(customerId)
            } else {
                repository.getAll()
            }
            call.respond(projects)
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing project ID")
            )

            val project = repository.getById(id)
            if (project != null) {
                call.respond(project)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Project not found"))
            }
        }

        post {
            val project = call.receive<Project>()
            val newProject = project.copy(
                id = UUID.randomUUID().toString(),
                createdAt = Clock.System.now()
            )
            val created = repository.create(newProject)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing project ID")
            )

            val project = call.receive<Project>()
            try {
                val updated = repository.update(id, project)
                call.respond(updated)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing project ID")
            )

            val deleted = repository.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Project not found"))
            }
        }
    }
}
