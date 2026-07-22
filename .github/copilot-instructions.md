# Copilot Instructions for DroidMind

## Project Overview

**DroidMind** is an AI-powered Android device automation framework built with Kotlin Multiplatform. It interprets natural language commands to control Android devices/emulators through LLM integration (Google Gemini and Koog Agents).

### Architecture

The project follows a layered modular architecture:

- **composeApp**: Main desktop UI using Jetpack Compose, orchestrates the automation workflow
- **mind/** modules: Core intelligent automation logic
  - **agent**: LLM-based command interpreter and tool executor (uses Koog Agents framework)
  - **device**: Android device control via ADB, layout extraction, UI element discovery
  - **layout**: Device UI hierarchy analysis and element localization
  - **verifier**: Action verification by comparing device state before/after
  - **domain**: Shared domain models for agent-device communication
- **feature/automation-runner**: Business logic for running automation scenarios
- **data/domain**: Local state management and persistence (Room database)
- **core/**: Cross-cutting concerns (logging, configuration)
- **interaction-engine**: Secondary agent framework (Java-based)
- **acp-starter**: ACP (Agent Client Protocol) integration entry point

### Key Technologies

- **Kotlin Multiplatform**: Desktop JVM target, common business logic
- **Koog Agents**: LLM agent framework with tool calling
- **Compose Multiplatform**: Desktop UI
- **Koin 4.0**: Dependency injection with qualifiers and modules
- **Coroutines**: Async device operations
- **Android Tools**: ADB communication (adblib, sdklib)
- **Ktor**: HTTP client for API calls
- **Room/SQLite**: Local persistence
- **Appium**: Android device testing utilities

## Build & Commands

### Build the project
```bash
./gradlew build
```

### Run the desktop app
```bash
./gradlew composeApp:run
```

### Run a single module's tests
```bash
./gradlew mind:device:test
./gradlew mind:layout:test
```

### Run all tests
```bash
./gradlew test
```

### Lint and check (Kotlin code style)
```bash
# Check formatting
./gradlew ktfmtCheck

# Auto-format code
./gradlew ktfmt
```

### Run the ACP starter (Agent Client Protocol)
```bash
./gradlew acp-starter:run
```

### Clean build
```bash
./gradlew clean build
```

## Code Conventions

### Module Organization

Each module follows a standard structure:
```
module/
├── build.gradle.kts
├── src/
│   ├── commonMain/kotlin/com/cacaosd/droidmind/<module>/
│   │   ├── di/          # Koin modules and DI qualifiers
│   │   ├── domain/      # Core interfaces and models
│   │   ├── strategy/    # Implementation strategies
│   │   ├── tools/       # LLM tool definitions (in agent module)
│   │   └── ...
│   ├── desktopMain/kotlin/  # Desktop-specific implementations
│   └── desktopTest/kotlin/  # Desktop tests
```

### Dependency Injection with Koin

Each module defines a `di/` directory containing:
- `<ModuleName>Module.kt`: Main Koin module with `val <moduleName>Module = module { ... }`
- `*Qualifier.kt`: Custom qualifier objects extending `SelfResolveQualifier` for multi-instance disambiguation

Example:
```kotlin
// mind/device/di/DeviceModule.kt
data object AndroidDeviceControllerQualifier : SelfResolveQualifier()

val deviceModule = module {
    single(AndroidDeviceControllerQualifier) { DefaultAndroidDeviceController(...) }
}

// Usage in other modules:
single { DeviceControllerTools(deviceController = get(AndroidDeviceControllerQualifier)) }
```

### Package Naming

Base package for all modules: `com.cacaosd.droidmind.<module_short_name>`

Examples:
- `com.cacaosd.droidmind.agent` (mind:agent)
- `com.cacaosd.droidmind.mind.device` (mind:device)
- `com.cacaosd.droidmind.mind.layout` (mind:layout)
- `com.cacaosd.droidmind.core.config` (core:config)
- `com.cacaosd.droidmind.data_local` (data:local)

### LLM Tool Definition (Agent Module)

Tools for LLM-based device control are defined using the Koog Agents framework:
- Located in `mind/agent/src/commonMain/kotlin/com/cacaosd/droidmind/agent/tools/`
- Tool classes extend or work with `ToolRegistry`
- Methods decorated with tool annotations define LLM-callable operations
- Must include proper descriptions for LLM understanding

Example:
```kotlin
class DeviceControllerTools(private val deviceController: AndroidDeviceController) {
    // Tool methods with descriptive names and parameters
    // Koog framework handles LLM invocation
}
```

### Coroutines & Async Patterns

- Use `suspend fun` for async device operations (ADB calls, UI verification)
- Leverage `coroutineScope { }` for structured concurrency in `main()` functions
- Device operations may block; use `PlatformDispatchers` from `com.cacaosd.platform.coroutines.dispatchers` (multiplatform dispatcher wrapper, not `Dispatchers.IO`)
- Event flows use `MutableSharedFlow` for reactive state updates
- Inject `PlatformDispatchers` via Koin for consistent dispatcher usage across modules

### Configuration & Qualifiers

- Use `SelfResolveQualifier` for disambiguating multiple implementations of the same interface
- Properties are injected via `Properties` object (injected as single in CoreConfigModule)
- Android device configurations loaded through `AndroidDeviceConfigurator` pattern

### Testing

- Desktop-only tests in `desktopTest/kotlin/`
- Unit tests verify tool execution, device communication, and UI layout analysis
- Use `kotlin.test` library (from version catalog)
- Run individual module tests with `./gradlew <module>:desktopTest`

## Important Patterns

### Device Interaction Flow

1. **Command Interpretation** (agent module): LLM interprets user intent
2. **Tool Execution** (agent tools): Tools call device controller methods
3. **Device Control** (device module): ADB commands execute on device
4. **Layout Analysis** (layout module): UI hierarchy parsed for element discovery
5. **Verification** (verifier module): State compared before/after action
6. **Feedback** (agent event flow): Results returned to LLM for next step

### Adding New Device Capabilities

1. Add tool method in `mind/agent/tools/<Domain>Tools.kt`
2. Inject `AndroidDeviceController` from device module
3. Implement capability in `mind/device` (ADB command wrapping)
4. Register tool in `agentToolsModule` (Koin)
5. Provide descriptive documentation for LLM

### Custom Gradle Plugins & Dependencies

- Version catalog managed in `gradle/libs.versions.toml`
- All dependencies declared there (no hardcoded versions in build.gradle.kts)
- Use `alias(libs.plugins.*)` for plugins
- Use `alias(libs.*)` for library dependencies

## Important Notes

- **ADB Integration**: This project requires Android Debug Bridge tools; ensure ADB is in PATH or configured
- **AI Safety**: Tool execution happens in response to LLM interpretation—test automation scenarios carefully
- **Multiplatform**: Common logic in `commonMain`, desktop-specific in `desktopMain`
- **Gradle Build Cache**: Enabled (`org.gradle.caching=true`) for faster builds
- **Configuration Cache**: Enabled (`org.gradle.configuration-cache=true`) for faster builds after first run
