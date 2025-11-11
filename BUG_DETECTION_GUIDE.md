# Comprehensive Bug Detection Guide

This guide outlines strategies and tools to systematically catch bugs in the Alter RSPS codebase.

---

## Table of Contents

1. [Static Code Analysis](#static-code-analysis)
2. [Automated Testing](#automated-testing)
3. [Code Review Practices](#code-review-practices)
4. [Runtime Monitoring](#runtime-monitoring)
5. [CI/CD Integration](#cicd-integration)
6. [Best Practices](#best-practices)

---

## 1. Static Code Analysis

Static analysis tools examine code without executing it, catching bugs before runtime.

### 1.1 Detekt (Kotlin Static Analysis)

**What it catches:**
- Code smells
- Potential bugs
- Complexity issues
- Code style violations
- Performance issues

**Setup:**

Add to `build.gradle.kts`:
```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
}

detekt {
    buildUponDefaultConfig = true
    allRules = true
    config.setFrom("$projectDir/config/detekt.yml")
}

tasks.named("detekt").configure {
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(true)
    }
}
```

**Create `config/detekt.yml`:**
```yaml
complexity:
  LongMethod:
    threshold: 50
  LongParameterList:
    threshold: 8
  CyclomaticComplexMethod:
    threshold: 15

potential-bugs:
  UnreachableCode:
    active: true
  UnusedPrivateMember:
    active: true
  UnusedVariable:
    active: true
  UnconditionalJumpStatementInLoop:
    active: true

style:
  MagicNumber:
    ignoreNumbers: ['-1', '0', '1', '2']
  MaxLineLength:
    maxLineLength: 120
```

**Run:**
```bash
./gradlew detekt
```

### 1.2 ktlint (Kotlin Linter)

**What it catches:**
- Code formatting issues
- Style violations
- Inconsistent code style

**Setup:**

Add to `build.gradle.kts`:
```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

ktlint {
    version.set("1.0.0")
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
```

**Run:**
```bash
./gradlew ktlintCheck
./gradlew ktlintFormat  # Auto-fix issues
```

### 1.3 SpotBugs (Java/Kotlin Bug Finder)

**What it catches:**
- Null pointer dereferences
- Infinite loops
- Dead code
- Resource leaks
- Thread safety issues

**Setup:**

Add to `build.gradle.kts`:
```kotlin
plugins {
    id("com.github.spotbugs") version "5.0.14"
}

spotbugs {
    toolVersion.set("4.8.0")
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.MEDIUM)
    excludeFilter.set(file("$projectDir/config/spotbugs-exclude.xml"))
}
```

**Run:**
```bash
./gradlew spotbugsMain
```

---

## 2. Automated Testing

### 2.1 Unit Tests

**What to test:**
- Individual functions and methods
- Edge cases
- Boundary conditions
- Error handling

**Example Test:**
```kotlin
class PawnListTests {
    @Test
    fun `add should use index 0`() {
        val list = PawnList(arrayOfNulls<Player>(10))
        val player = mockPlayer()
        
        val result = list.add(player)
        
        assertTrue(result)
        assertEquals(0, player.index) // Verify index 0 is used
        assertNotNull(list[0])
    }
    
    @Test
    fun `add should return false when full`() {
        val list = PawnList(arrayOfNulls<Player>(1))
        list.add(mockPlayer())
        
        val result = list.add(mockPlayer())
        
        assertFalse(result)
    }
}
```

**Run:**
```bash
./gradlew test
```

### 2.2 Integration Tests

**What to test:**
- Component interactions
- Plugin system
- Event handlers
- Database operations

**Example Test:**
```kotlin
class SocialSystemTests {
    @Test
    fun `pushFriends should not always return true`() {
        val social = Social()
        val player = mockPlayer()
        
        social.addFriend(player, "testfriend")
        social.pushFriends(player)
        
        // Verify friend list is processed correctly
        // (not just always returning FriendListLoaded)
    }
}
```

### 2.3 Property-Based Testing

Use libraries like **Kotest** for property-based testing:

```kotlin
class LootTableTests {
    @Test
    fun `preRoll should never crash with null weights`() {
        checkAll(Arb.list(Arb.intOrNull())) { weights ->
            val lootTable = createLootTable(weights)
            // Should not throw NPE
            lootTable.preRoll()
        }
    }
}
```

### 2.4 Mutation Testing

Use **PIT** or **Stryker** to verify test quality:

```bash
./gradlew pitest
```

---

## 3. Code Review Practices

### 3.1 Pre-Commit Checklist

Before committing, check:

- [ ] No `println` statements
- [ ] No `TODO` without explanation
- [ ] No unsafe `!!` operators
- [ ] No empty catch blocks
- [ ] All functions have null checks where needed
- [ ] Array/list access is bounds-checked
- [ ] Error handling is proper
- [ ] Code compiles without warnings

### 3.2 Code Review Guidelines

**Review for:**
1. **Null Safety**
   - Look for `!!` operators
   - Check nullable types are handled
   - Verify null checks before access

2. **Logic Errors**
   - Check for `|| true` or `&& false`
   - Verify loop conditions
   - Check array bounds

3. **Resource Management**
   - File handles closed
   - Network connections closed
   - Memory leaks

4. **Thread Safety**
   - Shared state access
   - Concurrent modifications
   - Race conditions

5. **Error Handling**
   - Exceptions caught appropriately
   - Error messages are helpful
   - Failures are logged

### 3.3 Automated Pre-Commit Hooks

Create `.git/hooks/pre-commit`:
```bash
#!/bin/bash
set -e

echo "Running ktlint..."
./gradlew ktlintCheck

echo "Running detekt..."
./gradlew detekt

echo "Running tests..."
./gradlew test

echo "All checks passed!"
```

---

## 4. Runtime Monitoring

### 4.1 Exception Tracking

**Add exception logging:**
```kotlin
try {
    // risky code
} catch (e: Exception) {
    logger.error(e) { 
        "Error in ${this::class.simpleName}: ${e.message}" 
    }
    // Don't just printStackTrace()
}
```

### 4.2 Null Pointer Tracking

**Add null checks with logging:**
```kotlin
val value = nullableValue ?: run {
    logger.warn { "Expected non-null value but got null in ${this::class.simpleName}" }
    return@run defaultValue
}
```

### 4.3 Assertions

**Use assertions for invariants:**
```kotlin
fun add(pawn: T): Boolean {
    require(pawn.index == -1) { "Pawn already has index: ${pawn.index}" }
    
    for (i in 0 until pawns.size) {
        // ...
    }
}
```

### 4.4 Performance Monitoring

**Track slow operations:**
```kotlin
val start = System.currentTimeMillis()
// operation
val duration = System.currentTimeMillis() - start
if (duration > 100) {
    logger.warn { "Slow operation took ${duration}ms" }
}
```

---

## 5. CI/CD Integration

### 5.1 GitHub Actions

Create `.github/workflows/ci.yml`:
```yaml
name: CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Run detekt
        run: ./gradlew detekt
      - name: Run ktlint
        run: ./gradlew ktlintCheck
      - name: Run spotbugs
        run: ./gradlew spotbugsMain
```

### 5.2 Automated Bug Detection

**Set up automated scans:**
- Run all static analysis tools on every PR
- Fail builds if critical issues found
- Generate reports for review

---

## 6. Best Practices

### 6.1 Null Safety

**DO:**
```kotlin
val value = nullableValue ?: defaultValue
val result = nullableValue?.let { process(it) } ?: return
```

**DON'T:**
```kotlin
val value = nullableValue!!  // Can crash
val result = nullableValue?.let { process(it) }!!  // Can crash
```

### 6.2 Error Handling

**DO:**
```kotlin
try {
    riskyOperation()
} catch (e: SpecificException) {
    logger.error(e) { "Context: what went wrong" }
    handleError()
}
```

**DON'T:**
```kotlin
try {
    riskyOperation()
} catch (e: Exception) {
    e.printStackTrace()  // No context, no handling
}
```

### 6.3 Debug Code

**DO:**
```kotlin
if (logger.isDebugEnabled) {
    logger.debug { "Debug info: $data" }
}
```

**DON'T:**
```kotlin
println("Debug: $data")  // Left in production
```

### 6.4 Array/List Access

**DO:**
```kotlin
if (index in 0 until list.size) {
    val item = list[index]
}
// or
val item = list.getOrNull(index) ?: return
```

**DON'T:**
```kotlin
val item = list[index]!!  // Can crash
```

### 6.5 Logic Checks

**DO:**
```kotlin
if (condition) {
    // handle true case
} else {
    // handle false case
}
```

**DON'T:**
```kotlin
if (condition || true) {  // Always true!
    // ...
}
```

---

## 7. Quick Setup Script

Run this to set up all tools:

```bash
#!/bin/bash

# Add detekt
echo "Setting up detekt..."
# (Add to build.gradle.kts)

# Add ktlint
echo "Setting up ktlint..."
# (Already configured)

# Create config directories
mkdir -p config
mkdir -p .github/workflows

# Create detekt config
cat > config/detekt.yml << 'EOF'
# (detekt config from above)
EOF

# Create CI workflow
cat > .github/workflows/ci.yml << 'EOF'
# (CI config from above)
EOF

echo "Setup complete! Run './gradlew detekt ktlintCheck test' to verify."
```

---

## 8. Regular Maintenance

### Weekly Tasks:
- Review static analysis reports
- Fix high-priority issues
- Update test coverage

### Monthly Tasks:
- Review test coverage reports
- Update tool configurations
- Review and update best practices

### Quarterly Tasks:
- Major code review
- Performance analysis
- Security audit

---

## 9. Common Bug Patterns to Watch For

1. **Unsafe Null Assertions**: `!!` operator
2. **Always True Conditions**: `|| true`, `&& false`
3. **Array Index Bugs**: Starting from 1 instead of 0
4. **Empty Catch Blocks**: Silent failures
5. **Debug Code**: `println` in production
6. **Resource Leaks**: Unclosed files/connections
7. **Race Conditions**: Unsynchronized shared state
8. **Magic Numbers**: Hard-coded values without constants
9. **Dead Code**: Unreachable code paths
10. **Infinite Loops**: Missing loop termination conditions

---

## 10. Tools Summary

| Tool | Purpose | Command |
|------|---------|---------|
| **detekt** | Kotlin static analysis | `./gradlew detekt` |
| **ktlint** | Code formatting | `./gradlew ktlintCheck` |
| **SpotBugs** | Bug finder | `./gradlew spotbugsMain` |
| **JUnit** | Unit testing | `./gradlew test` |
| **Kotest** | Advanced testing | `./gradlew test` |
| **PIT** | Mutation testing | `./gradlew pitest` |

---

## Next Steps

1. **Immediate**: Set up detekt and ktlint
2. **Short-term**: Add more unit tests for critical paths
3. **Medium-term**: Set up CI/CD pipeline
4. **Long-term**: Achieve 80%+ test coverage

---

## Resources

- [Detekt Documentation](https://detekt.github.io/detekt/)
- [ktlint Documentation](https://ktlint.github.io/)
- [SpotBugs Documentation](https://spotbugs.github.io/)
- [Kotlin Testing Guide](https://kotlinlang.org/docs/jvm-test-using-junit.html)

