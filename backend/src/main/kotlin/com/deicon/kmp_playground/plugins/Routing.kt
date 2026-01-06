package com.deicon.kmp_playground.plugins

import com.deicon.kmp_playground.repository.*
import com.deicon.kmp_playground.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // Initialize repositories
    val customerRepository = CustomerRepositoryImpl(dataSource)
    val projectRepository = ProjectRepositoryImpl(dataSource)
    val todoRepository = TodoRepositoryImpl(dataSource)

    routing {
        get("/") {
            call.respondText("KMP Playground Backend API")
        }

        get("/health") {
            call.respond(mapOf("status" to "OK"))
        }

        customerRoutes(customerRepository)
        projectRoutes(projectRepository)
        todoRoutes(todoRepository)
    }
}
