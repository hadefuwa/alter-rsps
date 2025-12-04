# Client Editor - Proper Workflow for Editing RuneLite Client

This tool provides a proper workflow for making changes to the RuneLite client JAR file.

## The Problem with Decompile/Edit/Recompile

When you decompile a JAR file:
- The decompiled code often has syntax errors
- Many dependencies are missing
- Compilation fails with 100+ errors
- Hard to maintain

## The Solution: Direct JAR Modification

Instead of decompiling, we modify the JAR directly using bytecode tools or by replacing specific strings/classes.

## Quick Start

### For Simple String Changes (Like Window Title)

**Option 1: Hex Editor (Fastest for one-off changes)**
1. Open `libs/custom-runelite-client.jar` in HxD
2. Search for the string you want to change
3. Replace it (must be same length or shorter)
4. Save

**Option 2: Use a JAR string replacement tool**
- Tools like `jar` command can extract, modify, and repackage
- Or use specialized JAR editing tools

### For Code Changes (Multiple Methods/Classes)

**Recommended: Build from RuneLite Source**

1. Clone RuneLite repository
2. Make your changes in source
3. Build the client
4. Use the built JAR

This is the most proper approach for extensive changes.

## Current Status

The `client-editor` module is set up but needs implementation. For now, use:
- Hex editor for simple string changes
- RuneLite source build for extensive changes


