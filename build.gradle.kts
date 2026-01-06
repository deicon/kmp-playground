plugins {
    // Kotlin
    kotlin("multiplatform") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
    
    // Compose
    id("org.jetbrains.compose") version "1.7.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false
    
    // SQLDelight
    id("app.cash.sqldelight") version "2.0.2" apply false
}

allprojects {
    group = "com.deicon.kmp_playground"
    version = "1.0.0"
}
