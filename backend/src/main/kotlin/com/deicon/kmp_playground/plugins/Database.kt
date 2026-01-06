package com.deicon.kmp_playground.plugins

import com.deicon.kmp_playground.config.createDataSource
import com.deicon.kmp_playground.config.getDatabaseConfiguration
import com.deicon.kmp_playground.config.initializeDatabase
import io.ktor.server.application.*
import javax.sql.DataSource

lateinit var dataSource: DataSource
    private set

fun Application.configureDatabase() {
    val dbConfig = getDatabaseConfiguration()
    dataSource = createDataSource(dbConfig)

    // Initialize database tables
    try {
        dataSource.initializeDatabase()
        log.info("Database initialized successfully")
    } catch (e: Exception) {
        log.error("Failed to initialize database", e)
        throw e
    }
}
