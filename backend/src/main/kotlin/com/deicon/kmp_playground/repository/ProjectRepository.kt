package com.deicon.kmp_playground.repository

import com.deicon.kmp_playground.models.Project
import kotlinx.datetime.Instant
import javax.sql.DataSource

interface ProjectRepository {
    suspend fun getAll(): List<Project>
    suspend fun getById(id: String): Project?
    suspend fun getByCustomerId(customerId: String): List<Project>
    suspend fun create(project: Project): Project
    suspend fun update(id: String, project: Project): Project
    suspend fun delete(id: String): Boolean
}

class ProjectRepositoryImpl(private val dataSource: DataSource) : ProjectRepository {

    override suspend fun getAll(): List<Project> {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM project ORDER BY created_at DESC").use { stmt ->
                val rs = stmt.executeQuery()
                val projects = mutableListOf<Project>()
                while (rs.next()) {
                    projects.add(
                        Project(
                            id = rs.getString("id"),
                            customerId = rs.getString("customer_id"),
                            name = rs.getString("name"),
                            description = rs.getString("description"),
                            createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at"))
                        )
                    )
                }
                projects
            }
        }
    }

    override suspend fun getById(id: String): Project? {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM project WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    Project(
                        id = rs.getString("id"),
                        customerId = rs.getString("customer_id"),
                        name = rs.getString("name"),
                        description = rs.getString("description"),
                        createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at"))
                    )
                } else {
                    null
                }
            }
        }
    }

    override suspend fun getByCustomerId(customerId: String): List<Project> {
        return dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT * FROM project WHERE customer_id = ? ORDER BY created_at DESC"
            ).use { stmt ->
                stmt.setString(1, customerId)
                val rs = stmt.executeQuery()
                val projects = mutableListOf<Project>()
                while (rs.next()) {
                    projects.add(
                        Project(
                            id = rs.getString("id"),
                            customerId = rs.getString("customer_id"),
                            name = rs.getString("name"),
                            description = rs.getString("description"),
                            createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at"))
                        )
                    )
                }
                projects
            }
        }
    }

    override suspend fun create(project: Project): Project {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "INSERT INTO project (id, customer_id, name, description, created_at) VALUES (?, ?, ?, ?, ?)"
            ).use { stmt ->
                stmt.setString(1, project.id)
                stmt.setString(2, project.customerId)
                stmt.setString(3, project.name)
                stmt.setString(4, project.description)
                stmt.setLong(5, project.createdAt.toEpochMilliseconds())
                stmt.executeUpdate()
            }
        }
        return project
    }

    override suspend fun update(id: String, project: Project): Project {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "UPDATE project SET customer_id = ?, name = ?, description = ?, created_at = ? WHERE id = ?"
            ).use { stmt ->
                stmt.setString(1, project.customerId)
                stmt.setString(2, project.name)
                stmt.setString(3, project.description)
                stmt.setLong(4, project.createdAt.toEpochMilliseconds())
                stmt.setString(5, id)
                val updated = stmt.executeUpdate()
                if (updated == 0) {
                    throw NoSuchElementException("Project not found: $id")
                }
            }
        }
        return project.copy(id = id)
    }

    override suspend fun delete(id: String): Boolean {
        return dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM project WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                stmt.executeUpdate() > 0
            }
        }
    }
}
