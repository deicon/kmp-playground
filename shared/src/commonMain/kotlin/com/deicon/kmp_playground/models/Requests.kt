package com.deicon.kmp_playground.models

import kotlinx.serialization.Serializable

/**
 * Request DTOs for creating and updating entities.
 * These are used by the API to avoid requiring clients to generate IDs and timestamps.
 */

@Serializable
data class CreateCustomerRequest(
    val name: String,
    val email: String? = null
)

@Serializable
data class UpdateCustomerRequest(
    val name: String,
    val email: String? = null
)

@Serializable
data class CreateProjectRequest(
    val customerId: String,
    val name: String,
    val description: String? = null
)

@Serializable
data class UpdateProjectRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class CreateTodoRequest(
    val projectId: String,
    val title: String,
    val description: String? = null,
    val estimatedHours: Double? = null
)

@Serializable
data class UpdateTodoRequest(
    val title: String,
    val description: String? = null,
    val estimatedHours: Double? = null
)
