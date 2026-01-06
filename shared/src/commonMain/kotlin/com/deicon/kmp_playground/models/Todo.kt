package com.deicon.kmp_playground.models

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Todo entity representing a task within a project.
 * Tracks estimated vs actual time spent in decimal hours.
 */
@Serializable
data class Todo(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val estimatedHours: Double? = null,
    val actualHours: Double = 0.0,
    val completed: Boolean = false,
    val createdAt: Instant,
    val completedAt: Instant? = null
) {
    init {
        require(title.isNotBlank()) { "Todo title cannot be blank" }
        estimatedHours?.let {
            require(it >= 0.0) { "Estimated hours must be non-negative" }
        }
        require(actualHours >= 0.0) { "Actual hours must be non-negative" }
        if (completed) {
            require(completedAt != null) { "Completed todos must have a completion timestamp" }
        }
    }
}
