package com.deicon.kmp_playground.repository

import com.deicon.kmp_playground.api.ProjectTimeStats
import com.deicon.kmp_playground.models.Todo
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.sql.DataSource

interface TodoRepository {
    suspend fun getAll(): List<Todo>
    suspend fun getById(id: String): Todo?
    suspend fun getByProjectId(projectId: String): List<Todo>
    suspend fun getCompleted(): List<Todo>
    suspend fun getIncomplete(): List<Todo>
    suspend fun create(todo: Todo): Todo
    suspend fun update(id: String, todo: Todo): Todo
    suspend fun updateActualHours(id: String, actualHours: Double): Todo
    suspend fun markCompleted(id: String): Todo
    suspend fun markIncomplete(id: String): Todo
    suspend fun delete(id: String): Boolean
    suspend fun getProjectTimeStats(projectId: String): ProjectTimeStats
}

class TodoRepositoryImpl(private val dataSource: DataSource) : TodoRepository {

    private fun mapRowToTodo(rs: java.sql.ResultSet): Todo {
        val dueDateMillis = rs.getLong("due_date")
        val completedAtMillis = rs.getLong("completed_at")

        return Todo(
            id = rs.getString("id"),
            projectId = rs.getString("project_id"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            dueDate = if (rs.wasNull()) null else LocalDate.fromEpochDays((dueDateMillis / (24 * 60 * 60 * 1000)).toInt()),
            estimatedHours = rs.getDouble("estimated_hours").let { if (rs.wasNull()) null else it },
            actualHours = rs.getDouble("actual_hours"),
            completed = rs.getBoolean("completed"),
            createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at")),
            completedAt = if (rs.wasNull()) null else Instant.fromEpochMilliseconds(completedAtMillis)
        )
    }

    override suspend fun getAll(): List<Todo> {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM todo ORDER BY created_at DESC").use { stmt ->
                val rs = stmt.executeQuery()
                val todos = mutableListOf<Todo>()
                while (rs.next()) {
                    todos.add(mapRowToTodo(rs))
                }
                todos
            }
        }
    }

    override suspend fun getById(id: String): Todo? {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM todo WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) mapRowToTodo(rs) else null
            }
        }
    }

    override suspend fun getByProjectId(projectId: String): List<Todo> {
        return dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT * FROM todo WHERE project_id = ? ORDER BY due_date ASC NULLS LAST, created_at DESC"
            ).use { stmt ->
                stmt.setString(1, projectId)
                val rs = stmt.executeQuery()
                val todos = mutableListOf<Todo>()
                while (rs.next()) {
                    todos.add(mapRowToTodo(rs))
                }
                todos
            }
        }
    }

    override suspend fun getCompleted(): List<Todo> {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM todo WHERE completed = TRUE ORDER BY completed_at DESC").use { stmt ->
                val rs = stmt.executeQuery()
                val todos = mutableListOf<Todo>()
                while (rs.next()) {
                    todos.add(mapRowToTodo(rs))
                }
                todos
            }
        }
    }

    override suspend fun getIncomplete(): List<Todo> {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM todo WHERE completed = FALSE ORDER BY due_date ASC NULLS LAST").use { stmt ->
                val rs = stmt.executeQuery()
                val todos = mutableListOf<Todo>()
                while (rs.next()) {
                    todos.add(mapRowToTodo(rs))
                }
                todos
            }
        }
    }

    override suspend fun create(todo: Todo): Todo {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """INSERT INTO todo (id, project_id, title, description, due_date, estimated_hours, actual_hours, completed, created_at, completed_at)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"""
            ).use { stmt ->
                stmt.setString(1, todo.id)
                stmt.setString(2, todo.projectId)
                stmt.setString(3, todo.title)
                stmt.setString(4, todo.description)

                val dueDate = todo.dueDate
                if (dueDate != null) {
                    stmt.setLong(5, dueDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
                } else {
                    stmt.setNull(5, java.sql.Types.BIGINT)
                }

                val estimatedHours = todo.estimatedHours
                if (estimatedHours != null) {
                    stmt.setDouble(6, estimatedHours)
                } else {
                    stmt.setNull(6, java.sql.Types.DOUBLE)
                }

                stmt.setDouble(7, todo.actualHours)
                stmt.setBoolean(8, todo.completed)
                stmt.setLong(9, todo.createdAt.toEpochMilliseconds())

                val completedAt = todo.completedAt
                if (completedAt != null) {
                    stmt.setLong(10, completedAt.toEpochMilliseconds())
                } else {
                    stmt.setNull(10, java.sql.Types.BIGINT)
                }

                stmt.executeUpdate()
            }
        }
        return todo
    }

    override suspend fun update(id: String, todo: Todo): Todo {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """UPDATE todo SET project_id = ?, title = ?, description = ?, due_date = ?,
                   estimated_hours = ?, actual_hours = ?, completed = ?, created_at = ?, completed_at = ?
                   WHERE id = ?"""
            ).use { stmt ->
                stmt.setString(1, todo.projectId)
                stmt.setString(2, todo.title)
                stmt.setString(3, todo.description)

                val dueDate = todo.dueDate
                if (dueDate != null) {
                    stmt.setLong(4, dueDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
                } else {
                    stmt.setNull(4, java.sql.Types.BIGINT)
                }

                val estimatedHours = todo.estimatedHours
                if (estimatedHours != null) {
                    stmt.setDouble(5, estimatedHours)
                } else {
                    stmt.setNull(5, java.sql.Types.DOUBLE)
                }

                stmt.setDouble(6, todo.actualHours)
                stmt.setBoolean(7, todo.completed)
                stmt.setLong(8, todo.createdAt.toEpochMilliseconds())

                val completedAt = todo.completedAt
                if (completedAt != null) {
                    stmt.setLong(9, completedAt.toEpochMilliseconds())
                } else {
                    stmt.setNull(9, java.sql.Types.BIGINT)
                }

                stmt.setString(10, id)
                val updated = stmt.executeUpdate()
                if (updated == 0) {
                    throw NoSuchElementException("Todo not found: $id")
                }
            }
        }
        return todo.copy(id = id)
    }

    override suspend fun updateActualHours(id: String, actualHours: Double): Todo {
        val todo = getById(id) ?: throw NoSuchElementException("Todo not found: $id")
        return update(id, todo.copy(actualHours = actualHours))
    }

    override suspend fun markCompleted(id: String): Todo {
        val todo = getById(id) ?: throw NoSuchElementException("Todo not found: $id")
        return update(id, todo.copy(completed = true, completedAt = kotlinx.datetime.Clock.System.now()))
    }

    override suspend fun markIncomplete(id: String): Todo {
        val todo = getById(id) ?: throw NoSuchElementException("Todo not found: $id")
        return update(id, todo.copy(completed = false, completedAt = null))
    }

    override suspend fun delete(id: String): Boolean {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM todo WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                stmt.executeUpdate() > 0
            }
        }
    }

    override suspend fun getProjectTimeStats(projectId: String): ProjectTimeStats {
        return dataSource.connection.use { conn ->
            conn.prepareStatement(
                """SELECT
                    COALESCE(SUM(estimated_hours), 0.0) as total_estimated,
                    COALESCE(SUM(actual_hours), 0.0) as total_actual,
                    COUNT(*) as total_count,
                    COUNT(CASE WHEN completed = TRUE THEN 1 END) as completed_count
                   FROM todo WHERE project_id = ?"""
            ).use { stmt ->
                stmt.setString(1, projectId)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    ProjectTimeStats(
                        projectId = projectId,
                        totalEstimatedHours = rs.getDouble("total_estimated"),
                        totalActualHours = rs.getDouble("total_actual"),
                        todoCount = rs.getInt("total_count"),
                        completedTodoCount = rs.getInt("completed_count")
                    )
                } else {
                    ProjectTimeStats(projectId, 0.0, 0.0, 0, 0)
                }
            }
        }
    }
}
