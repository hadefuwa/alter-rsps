# Git Tags Guide - Rollback Strategy

This guide explains how to use Git tags to mark stable releases and roll back to known good states if the code breaks.

## Current Tags

- **v0.0.5-stable** - Stable release after bug fixes and incomplete code documentation
  - All critical bugs fixed
  - Comprehensive documentation added
  - All incomplete code documented

## Creating Tags

### Annotated Tags (Recommended)
Annotated tags store extra metadata and are recommended for releases:

```bash
# Create an annotated tag
git tag -a v0.0.6-stable -m "Description of what's in this release"

# Push tag to GitHub
git push origin v0.0.6-stable
```

### Lightweight Tags
Lightweight tags are just pointers to commits:

```bash
# Create a lightweight tag
git tag v0.0.6-stable

# Push tag to GitHub
git push origin v0.0.6-stable
```

## Tag Naming Convention

Recommended naming convention:

- **v0.0.5-stable** - Stable release
- **v0.0.5-beta** - Beta release
- **v0.0.5-alpha** - Alpha release
- **v0.0.5-feature-xyz** - Feature-specific tag
- **v0.0.5-bugfix-abc** - Bug fix tag

Format: `v<major>.<minor>.<patch>-<type>`

## Viewing Tags

```bash
# List all tags
git tag

# List tags with messages
git tag -l -n

# Show specific tag details
git show v0.0.5-stable
```

## Rolling Back to a Tag

### Option 1: Create a New Branch from Tag (Recommended)
This preserves your current work and allows you to continue development:

```bash
# Create a new branch from a tag
git checkout -b rollback-v0.0.5-stable v0.0.5-stable

# Push the branch
git push origin rollback-v0.0.5-stable
```

### Option 2: Reset Current Branch to Tag (Destructive)
⚠️ **WARNING:** This will lose all commits after the tag!

```bash
# Reset current branch to tag (hard reset - loses uncommitted changes)
git reset --hard v0.0.5-stable

# Force push (only if you're sure!)
git push origin main --force
```

### Option 3: Checkout Tag (Read-Only)
This puts you in "detached HEAD" state - good for testing:

```bash
# Checkout tag (read-only)
git checkout v0.0.5-stable

# Create a branch from here if you want to make changes
git checkout -b fix-from-stable
```

## Best Practices

### When to Create Tags

1. **After major bug fixes** - Tag stable versions
2. **Before major changes** - Tag before risky refactoring
3. **After feature completion** - Tag milestone releases
4. **Before deployment** - Tag production releases

### Tagging Workflow

```bash
# 1. Ensure code is committed
git status

# 2. Create tag
git tag -a v0.0.6-stable -m "Description"

# 3. Push tag to GitHub
git push origin v0.0.6-stable

# 4. Verify tag exists
git tag -l
```

## Example Scenarios

### Scenario 1: Code Breaks After Recent Changes

```bash
# 1. Check what tags are available
git tag -l

# 2. Create a branch from last known good tag
git checkout -b rollback-to-stable v0.0.5-stable

# 3. Test the rollback branch
# ... test your code ...

# 4. If stable, merge back to main (or replace main)
git checkout main
git merge rollback-to-stable

# Or replace main entirely:
git checkout main
git reset --hard v0.0.5-stable
git push origin main --force
```

### Scenario 2: Before Making Risky Changes

```bash
# 1. Tag current stable state
git tag -a v0.0.6-pre-refactor -m "Before risky refactoring"
git push origin v0.0.6-pre-refactor

# 2. Make your changes
# ... make changes ...

# 3. If something breaks, roll back
git reset --hard v0.0.6-pre-refactor
```

### Scenario 3: Release Management

```bash
# 1. Tag stable release
git tag -a v0.0.6-stable -m "Stable release v0.0.6"
git push origin v0.0.6-stable

# 2. Continue development on main
# ... continue working ...

# 3. If needed, checkout stable version
git checkout v0.0.6-stable
```

## Deleting Tags

```bash
# Delete local tag
git tag -d v0.0.5-stable

# Delete remote tag
git push origin --delete v0.0.5-stable
```

## Viewing Tag History

```bash
# Show commits between tags
git log v0.0.5-stable..HEAD

# Show what changed since tag
git diff v0.0.5-stable..HEAD

# Show tag in commit history
git log --oneline --decorate
```

## GitHub Integration

Tags pushed to GitHub will appear in:
- **Releases** section (if you create a GitHub Release)
- **Tags** tab in the repository
- Can be used to create releases with release notes

### Creating a GitHub Release from Tag

1. Go to your repository on GitHub
2. Click "Releases" → "Create a new release"
3. Select the tag (e.g., `v0.0.5-stable`)
4. Add release notes
5. Publish release

## Quick Reference

```bash
# Create tag
git tag -a <tag-name> -m "<message>"
git push origin <tag-name>

# List tags
git tag -l

# Rollback to tag (create branch)
git checkout -b <branch-name> <tag-name>

# Rollback to tag (reset current branch) - DESTRUCTIVE
git reset --hard <tag-name>

# Delete tag
git tag -d <tag-name>
git push origin --delete <tag-name>
```

## Current Stable Tag

**v0.0.5-stable** - Created after:
- All critical bugs fixed
- Comprehensive incomplete code documentation
- Bug detection guide added
- All code quality improvements

Use this tag to roll back if needed:
```bash
git checkout -b rollback-stable v0.0.5-stable
```

