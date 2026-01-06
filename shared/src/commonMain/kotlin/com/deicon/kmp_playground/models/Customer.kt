package com.deicon.kmp_playground.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Customer entity representing a client or organization.
 * Customers can have multiple projects.
 */
@Serializable
data class Customer(
    val id: String,
    val name: String,
    val email: String? = null,
    val createdAt: Instant
) {
    init {
        require(name.isNotBlank()) { "Customer name cannot be blank" }
        email?.let {
            require(it.contains("@")) { "Invalid email format" }
        }
    }
}
