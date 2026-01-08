# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

basicafx-kt is a JavaFX library for Kotlin that provides utilities, extensions, and architectural components for building JavaFX applications with Kotlin. The library is built on TornadoFX and provides additional functionality for JavaFX development.

**Key Technologies:**
- Kotlin 2.2.0
- JavaFX 21.0.2
- TornadoFX 1.7.20
- Java 17 (required)
- Gradle with Kotlin DSL

## Build Commands

### Building the project
```bash
./gradlew build
```

### Running tests
```bash
# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "io.github.nayasis.kotlin.javafx.app.di.SimpleDiContainerTest"

# Run a single test method
./gradlew test --tests "io.github.nayasis.kotlin.javafx.app.di.SimpleDiContainerTest.basic set and get"
```

### Publishing
```bash
# Publish to Maven Local
./gradlew publishToMavenLocal

# Publish to Maven Central (requires credentials and signing)
./gradlew publishMavenPublicationToMavenCentral
```

### Clean build
```bash
./gradlew clean build
```

## Architecture

### Core Application Framework

The library centers around `FxApp` (`io/github/nayasis/kotlin/javafx/app/FxApp.kt`), which extends TornadoFX's `App` class and provides:

1. **Dependency Injection**: Uses `SimpleDiContainer` for lightweight DI without external frameworks (Google Guice was recently removed)
2. **Environment Configuration**: `Environment` class loads configuration from `application.yml` and command-line arguments
3. **Exception Handling**: Default uncaught exception handlers that show JavaFX dialogs on errors
4. **Preloader Support**: Integration with `DefaultPreloader` for application splash screens

### Dependency Injection System

The DI system (`io/github/nayasis/kotlin/javafx/app/di/SimpleDiContainer.kt`) provides:

- **Annotation-based**: Classes annotated with `@Inject` can be automatically instantiated
- **Constructor injection**: Automatically resolves and injects constructor dependencies
- **Package scanning**: `scanPackages()` method finds and instantiates all `@Inject` annotated classes
- **Named beans**: Support for multiple instances of the same type with different names
- **Circular dependency detection**: Throws `IllegalStateException` if circular dependencies are detected

**Usage pattern:**
```kotlin
@Inject
class MyService(val dependency: OtherService)

// In FxApp
companion object {
    val ctx = SimpleDiContainer()
}

override fun init() {
    ctx.scanPackages("com.example.myapp")
    // or manually register
    ctx.set(MyService::class)
}
```

### Environment Configuration

`Environment` class (`io/github/nayasis/kotlin/javafx/app/Environment.kt`):
- Loads YAML configuration from `application.yml` resource
- Merges command-line arguments in `key=value` format
- Provides MVEL-like property access (e.g., `env["database.url"]`)
- Automatically registered in DI container during `FxApp.init()`

### Package Structure

- **`app/`**: Core application framework (FxApp, Environment, DI container)
- **`control/`**: Custom JavaFX controls and extensions
  - `basic/`: Button, Node, TextField extensions
  - `combobox/`: Custom combo box implementations
  - `glyph/`: Icon/glyph utilities
  - `tableview/`: TableView utilities and custom cells
- **`stage/`**: Window/dialog utilities
  - `Dialog`: Static utility for common dialogs (error, info, confirm, file choosers)
  - `progress/`: Progress dialog implementations
- **`fxml/`**: FXML loading utilities with field injection support
- **`preloader/`**: Application preloader/splash screen support
- **`misc/`**: Threading, images, desktop integration utilities
- **`scene/`**: Scene-related utilities
- **`property/`**: Custom JavaFX properties
- **`animation/`**: Animation utilities
- **`geometry/`**: Geometry-related utilities
- **`model/`**: Data models

## Testing

Tests use:
- **Kotest** for assertions (`shouldBe`, `shouldNotBe`, etc.)
- **JUnit 5** as the test runner
- **TestFX** for JavaFX UI testing

Tests are located in `src/test/kotlin` mirroring the main source structure.

## Development Notes

### TornadoFX Integration

This library extends TornadoFX, so many patterns follow TornadoFX conventions:
- Views extend `tornadofx.View`
- `UIComponent` is the base for UI components
- `App` is extended by `FxApp`
- TornadoFX's DI system is bridged to `SimpleDiContainer`

### FXML Loading

The `FxmlLoaders.kt` utilities provide automatic field injection when loading FXML:
- `FXMLLoader.loadWith(bean)` injects nodes with matching fx:id into bean properties
- `fxid()` delegate property for lazy node access by fx:id
- Automatic i18n with `Localizator`

### Dialog Utilities

`Dialog` companion object provides static methods for common dialogs:
- `Dialog.error(message, exception)` - Error dialog
- `Dialog.info(message)` - Information dialog
- `Dialog.confirm(message)` - Confirmation dialog
- `Dialog.chooseFile()` / `Dialog.chooseDirectory()` - File/directory choosers

### Version Management

Version is controlled by:
- `mavenReleaseVersion` property (for releases)
- Defaults to `0.1.0-SNAPSHOT` if not specified

Current published version: 0.2.3

### Git Conventions

**Commit Messages**: All commit messages must be written in English.

### Code Conventions

**Comments**: All code comments (including KDoc, inline comments, and block comments) must be written in English.
