#!/bin/bash
# Quick setup script - run this to create all directories

mkdir -p gradle/wrapper
mkdir -p shared/src/commonMain/kotlin/com/deicon/kmp_playground/models
mkdir -p shared/src/commonMain/kotlin/com/deicon/kmp_playground/utils
mkdir -p shared/src/commonMain/kotlin/com/deicon/kmp_playground/api
mkdir -p shared/src/commonTest/kotlin/com/deicon/kmp_playground/models
mkdir -p shared/src/commonTest/kotlin/com/deicon/kmp_playground/utils
mkdir -p shared/src/jvmMain/kotlin
mkdir -p shared/src/wasmJsMain/kotlin
mkdir -p shared/src/commonMain/sqldelight/com/deicon/kmp_playground/db
mkdir -p backend/src/main/kotlin/com/deicon/kmp_playground/routes
mkdir -p backend/src/main/kotlin/com/deicon/kmp_playground/repository
mkdir -p backend/src/main/kotlin/com/deicon/kmp_playground/config
mkdir -p backend/src/main/resources
mkdir -p backend/src/test/kotlin/com/deicon/kmp_playground
mkdir -p frontend/src/commonMain/kotlin/com/deicon/kmp_playground/ui
mkdir -p frontend/src/commonMain/kotlin/com/deicon/kmp_playground/viewmodel
mkdir -p frontend/src/commonMain/kotlin/com/deicon/kmp_playground/di
mkdir -p frontend/src/wasmJsMain/kotlin
mkdir -p frontend/src/wasmJsMain/resources
mkdir -p docker/nginx
mkdir -p e2e-tests/tests
mkdir -p scripts

echo "✓ Directory structure created successfully!"
echo ""
echo "Next steps:"
echo "1. Install Gradle 8.5+ if not already installed"
echo "2. Run: gradle wrapper (to generate ./gradlew)"
echo "3. Or if you have Gradle installed: ./gradlew build"
echo ""
echo "See TODO.md for files that need to be created next"

