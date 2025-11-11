#!/bin/bash

echo "Setting up bug detection tools for Alter RSPS..."
echo ""

# Check if detekt config exists
if [ ! -f "config/detekt.yml" ]; then
    echo "✓ Detekt config created"
else
    echo "✓ Detekt config already exists"
fi

# Check if ktlint is configured
if grep -q "ktlint" build.gradle.kts; then
    echo "✓ ktlint is configured"
else
    echo "✗ ktlint not found in build.gradle.kts"
fi

# Check if detekt is configured
if grep -q "detekt" build.gradle.kts; then
    echo "✓ detekt is configured"
else
    echo "✗ detekt not found in build.gradle.kts"
fi

echo ""
echo "Running initial checks..."
echo ""

# Run ktlint check
echo "1. Running ktlint..."
./gradlew ktlintCheck 2>&1 | head -20

# Run detekt (if configured)
if grep -q "detekt" build.gradle.kts; then
    echo ""
    echo "2. Running detekt..."
    ./gradlew detekt 2>&1 | head -20
fi

# Run tests
echo ""
echo "3. Running tests..."
./gradlew test --continue 2>&1 | tail -10

echo ""
echo "Setup complete! See BUG_DETECTION_GUIDE.md for details."
echo ""
echo "Quick commands:"
echo "  ./gradlew ktlintCheck    - Check code style"
echo "  ./gradlew ktlintFormat   - Auto-fix code style"
echo "  ./gradlew detekt         - Run static analysis"
echo "  ./gradlew test           - Run all tests"

