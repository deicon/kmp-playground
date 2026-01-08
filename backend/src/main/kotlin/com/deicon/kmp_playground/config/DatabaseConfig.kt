package com.deicon.kmp_playground.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import javax.sql.DataSource

data class DatabaseConfiguration(
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int
)

fun Application.getDatabaseConfiguration(): DatabaseConfiguration {
    return DatabaseConfiguration(
        host = System.getenv("DB_HOST") ?: "localhost",
        port = System.getenv("DB_PORT")?.toIntOrNull() ?: 5432,
        name = System.getenv("DB_NAME") ?: "todos",
        user = System.getenv("DB_USER") ?: "postgres",
        password = System.getenv("DB_PASSWORD") ?: "postgres",
        maxPoolSize = System.getenv("DB_MAX_POOL_SIZE")?.toIntOrNull() ?: 10
    )
}

fun createDataSource(config: DatabaseConfiguration): DataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.name}"
        username = config.user
        password = config.password
        maximumPoolSize = config.maxPoolSize
        driverClassName = "org.postgresql.Driver"

        // Performance settings
        connectionTimeout = 30000
        idleTimeout = 600000
        maxLifetime = 1800000

        // Validation
        connectionTestQuery = "SELECT 1"
        validationTimeout = 5000
    }

    return HikariDataSource(hikariConfig)
}

fun DataSource.initializeDatabase() {
    connection.use { conn ->
        conn.createStatement().use { statement ->
            // Create tables if they don't exist
            statement.execute("""
                CREATE TABLE IF NOT EXISTS customer (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT,
                    created_at BIGINT NOT NULL
                )
            """)

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_customer_name ON customer(name)
            """)

            statement.execute("""
                CREATE TABLE IF NOT EXISTS project (
                    id TEXT NOT NULL PRIMARY KEY,
                    customer_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    created_at BIGINT NOT NULL,
                    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
                )
            """)

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_project_customer_id ON project(customer_id)
            """)

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_project_name ON project(name)
            """)

            statement.execute("""
                CREATE TABLE IF NOT EXISTS todo (
                    id TEXT NOT NULL PRIMARY KEY,
                    project_id TEXT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    due_date BIGINT,
                    estimated_hours DOUBLE PRECISION,
                    actual_hours DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                    completed BOOLEAN NOT NULL DEFAULT FALSE,
                    created_at BIGINT NOT NULL,
                    completed_at BIGINT,
                    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
                )
            """)

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_todo_project_id ON todo(project_id)
            """)

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_todo_due_date ON todo(due_date)
            """)

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_todo_completed ON todo(completed)
            """)
        }
    }
}
