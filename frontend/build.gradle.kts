plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "frontend.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Shared module
                implementation(project(":shared"))

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)

                // Kotlinx libraries
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinx.coroutines.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("kotlinx.serialization.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${property("kotlinx.datetime.version")}")

                // Ktor client
                implementation("io.ktor:ktor-client-core:${property("ktor.version")}")
                implementation("io.ktor:ktor-client-content-negotiation:${property("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${property("ktor.version")}")

                // Dependency Injection
                implementation("io.insert-koin:koin-core:${property("koin.version")}")
            }
        }

        val wasmJsMain by getting {
            dependencies {
                // Ktor client for WASM
                implementation("io.ktor:ktor-client-js:${property("ktor.version")}")
            }
        }
    }
}

// Compose configuration (no longer experimental as of Compose 1.6.10+)
