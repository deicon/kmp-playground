plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass.set("com.deicon.kmp_playground.ApplicationKt")
}

dependencies {
    // Shared module
    implementation(project(":shared"))

    // Kotlinx libraries
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${property("kotlinx.datetime.version")}")

    // Ktor server
    implementation("io.ktor:ktor-server-core:${property("ktor.version")}")
    implementation("io.ktor:ktor-server-netty:${property("ktor.version")}")
    implementation("io.ktor:ktor-server-content-negotiation:${property("ktor.version")}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${property("ktor.version")}")
    implementation("io.ktor:ktor-server-cors:${property("ktor.version")}")
    implementation("io.ktor:ktor-server-call-logging:${property("ktor.version")}")
    implementation("io.ktor:ktor-server-status-pages:${property("ktor.version")}")

    // SQLDelight
    implementation("app.cash.sqldelight:jdbc-driver:${property("sqldelight.version")}")

    // PostgreSQL
    implementation("org.postgresql:postgresql:${property("postgresql.version")}")

    // HikariCP for connection pooling
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:${property("logback.version")}")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:${property("koin.version")}")
    implementation("io.insert-koin:koin-logger-slf4j:${property("koin.version")}")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:${property("ktor.version")}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:${property("kotlin.version")}")
    testImplementation("io.kotest:kotest-runner-junit5:${property("kotest.version")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotest.version")}")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
