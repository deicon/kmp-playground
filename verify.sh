#!/bin/bash
# Build verification script
# Run this after setup to ensure everything is configured correctly

set -e

echo "================================"
echo "KMP Playground - Build Verification"
echo "================================"
echo ""

# Check if directories exist
echo "[1/4] Checking directory structure..."
if [ -d "shared/src/commonMain/kotlin" ]; then
    echo "✓ Directory structure OK"
else
    echo "✗ Run ./setup.sh first to create directories"
    exit 1
fi

# Check Gradle wrapper
echo ""
echo "[2/4] Checking Gradle wrapper..."
if [ -f "gradlew" ]; then
    echo "✓ Gradle wrapper exists"
else
    echo "⚠ Gradle wrapper not found - will be downloaded on first build"
fi

# Build shared module
echo ""
echo "[3/4] Building shared module..."
./gradlew :shared:build --console=plain

# Run tests
echo ""
echo "[4/4] Running tests..."
./gradlew :shared:test --console=plain

echo ""
echo "================================"
echo "✓ Build verification complete!"
echo "================================"
echo ""
echo "Next steps:"
echo "1. Review PROJECT_PLAN.md for current progress"
echo "2. Continue with Phase 2: Backend Development"
echo "3. Or run: ./gradlew tasks --all (to see all available tasks)"
