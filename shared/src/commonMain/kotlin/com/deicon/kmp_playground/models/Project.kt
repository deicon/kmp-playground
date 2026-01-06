package com.deicon.kmp_playground.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Project entity representing a project belonging to a customer.
 * Projects can have multiple todos.
 */
@Serializable
data class Project(
    val id: String,
    val customerId: String,
    val name: String,
    val description: String? = null,
    val createdAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Project name cannot be blank" }
    }
}
