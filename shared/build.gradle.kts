plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(17)

    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Kotlinx libraries
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("kotlinx.serialization.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinx.coroutines.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${property("kotlinx.datetime.version")}")

                // Ktor client (for frontend)
                implementation("io.ktor:ktor-client-core:${property("ktor.version")}")
                implementation("io.ktor:ktor-client-content-negotiation:${property("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${property("ktor.version")}")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting

        val wasmJsMain by getting {
            dependencies {
                // Ktor client for WASM
                implementation("io.ktor:ktor-client-js:${property("ktor.version")}")
            }
        }
    }
}
