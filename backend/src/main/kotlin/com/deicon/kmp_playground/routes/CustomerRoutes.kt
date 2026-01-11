package com.deicon.kmp_playground.routes

import com.deicon.kmp_playground.models.CreateCustomerRequest
import com.deicon.kmp_playground.models.Customer
import com.deicon.kmp_playground.models.UpdateCustomerRequest
import com.deicon.kmp_playground.repository.CustomerRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import java.util.UUID

fun Route.customerRoutes(repository: CustomerRepository) {
    route("/api/customers") {

        get {
            val customers = repository.getAll()
            call.respond(customers)
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing customer ID")
            )

            val customer = repository.getById(id)
            if (customer != null) {
                call.respond(customer)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Customer not found"))
            }
        }

        post {
            val request = call.receive<CreateCustomerRequest>()
            val newCustomer = Customer(
                id = UUID.randomUUID().toString(),
                name = request.name,
                email = request.email,
                createdAt = Clock.System.now()
            )
            val created = repository.create(newCustomer)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing customer ID")
            )

            val request = call.receive<UpdateCustomerRequest>()
            val existing = repository.getById(id)
            if (existing == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Customer not found"))
                return@put
            }

            val updatedCustomer = existing.copy(
                name = request.name,
                email = request.email
            )
            try {
                val updated = repository.update(id, updatedCustomer)
                call.respond(updated)
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing customer ID")
            )

            val deleted = repository.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Customer not found"))
            }
        }
    }
}
