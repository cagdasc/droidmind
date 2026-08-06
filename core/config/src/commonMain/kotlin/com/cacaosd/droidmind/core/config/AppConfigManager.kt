package com.cacaosd.droidmind.core.config

import com.cacaosd.droidmind.core.config.models.AgentProviderConfig
import com.cacaosd.droidmind.core.config.models.AppConfig
import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Manages application configuration in JSON format
 * Following XDG Base Directory Specification and platform conventions
 */
class AppConfigManager(
    private val appName: String,
    private val appVersion: String = "1.0",
    private val packageName: String,
    private val json: Json
) {
    private val _settingsUpdatedFlow =
        MutableSharedFlow<AppConfig>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val settingsUpdatedFlow: SharedFlow<AppConfig> = _settingsUpdatedFlow.asSharedFlow()

    private var loadedConfig: AppConfig? = null

    fun initializeApp() {
        if (initialize() && isFirstRun()) {
            Logger.debug("This is your first time running the app.")
            markFirstRunCompleted()
        }
    }

    // Platform-specific base directories
    private val baseConfigDir: Path = when (getOperatingSystem()) {
        OS.WINDOWS -> Paths.get(System.getenv("APPDATA") ?: System.getProperty("user.home"), appName)
        OS.MACOS -> Paths.get(System.getProperty("user.home"), "Library", "Application Support", appName)
        OS.LINUX -> {
            val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
            if (xdgConfigHome != null) {
                Paths.get(xdgConfigHome, appName)
            } else {
                Paths.get(System.getProperty("user.home"), ".config", appName)
            }
        }

        OS.UNKNOWN -> Paths.get(System.getProperty("user.home"), ".${appName.lowercase()}")
    }

    // Application directories
    val configDir: Path = baseConfigDir
    val uiDumpDir: Path = baseConfigDir.resolve("ui_dump")
    val screenshotsDir: Path = baseConfigDir.resolve("screenshots")
    val storageDir: Path = baseConfigDir.resolve("storage")

    // Configuration files
    val mainConfigFile: Path = configDir.resolve("config.json")

    /**
     * Initialize all application directories and create default configuration files
     */
    private fun initialize(): Boolean {
        return try {
            createDirectoryStructure()
            createDefaultConfigFiles()
            loadConfig() // Load config on initialization
            Logger.debug("Application config manager initialized successfully")
            Logger.debug("Config directory: ${configDir.toAbsolutePath()}")
            true
        } catch (e: Exception) {
            Logger.error(message = "Failed to initialize config manager: ${e.message}", throwable = e)
            false
        }
    }

    /**
     * Create the complete directory structure
     */
    private fun createDirectoryStructure() {
        val directories = listOf(
            configDir,
            uiDumpDir,
            screenshotsDir,
            storageDir
        )

        directories.forEach { dir ->
            try {
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir)
                    Logger.info("Created directory: ${dir.toAbsolutePath()}")
                }

                if (!Files.isWritable(dir)) {
                    throw SecurityException("Directory is not writable: $dir")
                }
            } catch (e: Exception) {
                Logger.error("Failed to create directory: $dir", e)
                throw RuntimeException(e)
            }
        }
    }

    /**
     * Create default JSON configuration file if it doesn't exist
     */
    private fun createDefaultConfigFiles() {
        if (!Files.exists(mainConfigFile)) {
            val envVarExample = $$"${ENV_VAR_NAME}"
            val jsonContent = $$"""
                {
                  "_comment": "DroidMind Configuration File",
                  "app": {
                    "name": "droidmind",
                    "version": "0.0.1"
                  },
                  "api": {
                    "keys": {
                      "_comment": "Add your API keys here (supports env vars: \"${ENV_VAR_NAME}\")",
                      "gemini": "",
                      "ollama": ""
                    }
                  },
                  "agent": {
                    "_comment": "Agent provider configurations (optional - add only what you use).",
                    "providers": {
                      "google": {
                        "maxIterations": 250,
                        "temperature": 0.2
                      },
                      "ollama": {
                        "maxIterations": 100,
                        "temperature": 0.7,
                        "baseUrl": "http://localhost:11434"
                      }
                    }
                  }
                }
            """.trimIndent()

            Files.writeString(mainConfigFile, jsonContent)
            Logger.info("Created default config file: ${mainConfigFile.fileName}")
        }
    }

    /**
     * Load configuration from JSON file with environment variable substitution
     */
    private fun loadConfig(): AppConfig {
        return try {
            if (!Files.exists(mainConfigFile)) {
                Logger.info("Config file not found at ${mainConfigFile.toAbsolutePath()}, using defaults")
                loadedConfig = AppConfig()
                return loadedConfig!!
            }

            val jsonContent = Files.readString(mainConfigFile)
            val processedContent = substituteEnvironmentVariables(jsonContent)
            val config = json.decodeFromString<AppConfig>(processedContent)
            loadedConfig = config
            config
        } catch (e: Exception) {
            Logger.error("Failed to load config file: ${e.message}", throwable = e)
            AppConfig().also { loadedConfig = it }
        }
    }

    /**
     * Substitute environment variables in the format ${'$'}{ENV_VAR_NAME}
     */
    private fun substituteEnvironmentVariables(content: String): String {
        val pattern = Regex("""\$\{([A-Za-z_][A-Za-z0-9_]*)\}""")
        return content.replace(pattern) { matchResult ->
            val envVarName = matchResult.groupValues[1]
            System.getenv(envVarName) ?: matchResult.value
        }
    }

    /**
     * Get file path within a specific directory
     */
    fun getConfigFile(filename: String): Path = configDir.resolve(filename)
    fun getUiDumpFile(filename: String): Path = uiDumpDir.resolve(filename)
    fun getScreenshotsFile(filename: String): Path = screenshotsDir.resolve(filename)
    fun getStorageFile(filename: String): Path = storageDir.resolve(filename)

    /**
     * Check if this is the first run of the application
     */
    private fun isFirstRun(): Boolean {
        return !Files.exists(mainConfigFile)
    }

    /**
     * Mark first run as completed by updating the config file
     */
    private fun markFirstRunCompleted() {
        // First run marker is implicit - if config file exists, it's not first run
        Logger.info("First run setup completed")
    }

    /**
     * Get application info
     */
    fun getAppInfo(): AppInfo {
        return AppInfo(
            name = appName,
            version = appVersion,
            packageName = packageName,
            configDir = configDir.toAbsolutePath().toString(),
            isFirstRun = isFirstRun()
        )
    }

    /**
     * Save application settings (API keys)
     */
    fun saveApiKey(provider: String, apiKey: String): Boolean {
        return try {
            val config = loadedConfig ?: loadConfig()
            val updatedConfig = config.copy(
                api = config.api.copy(
                    keys = config.api.keys.toMutableMap().apply {
                        this[provider] = apiKey
                    }
                )
            )
            saveAppConfig(updatedConfig)
        } catch (e: Exception) {
            Logger.error("Failed to save app settings: ${e.message}", throwable = e)
            false
        }
    }

    /**
     * Save application settings (API keys)
     */
    fun saveEnvironmentVariable(key: String, value: String): Boolean {
        return try {
            val config = loadedConfig ?: loadConfig()
            val updatedConfig = config.copy(
                env = config.env.toMutableMap().apply {
                    this[key] = value
                }
            )
            saveAppConfig(updatedConfig)
        } catch (e: Exception) {
            Logger.error("Failed to save app settings: ${e.message}", throwable = e)
            false
        }
    }

    fun saveAppConfig(config: AppConfig): Boolean {
        return try {
            val jsonContent = json.encodeToString(config)
            Files.writeString(mainConfigFile, jsonContent)

            loadedConfig = config
            Logger.info("App settings saved successfully")

            _settingsUpdatedFlow.tryEmit(config)
            true
        } catch (e: Exception) {
            Logger.error("Failed to save app settings: ${e.message}", throwable = e)
            false
        }
    }

    /**
     * Get agent provider configuration by name
     * Returns null if provider is not configured
     */
    fun getAgentConfig(providerName: String): AgentProviderConfig? {
        return try {
            val config = loadedConfig ?: loadConfig()
            config.agent.providers[providerName]
        } catch (e: Exception) {
            Logger.error("Failed to get agent config for provider '$providerName': ${e.message}", throwable = e)
            null
        }
    }

    /**
     * Get API key by name
     */
    fun getApiKey(keyName: String): String? {
        return try {
            val config = loadedConfig ?: loadConfig()
            config.api.keys[keyName]?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Logger.error("Failed to get API key for '$keyName': ${e.message}", throwable = e)
            null
        }
    }

    /**
     * Get environment variable by name
     */
    fun getEnvironmentVariable(keyName: String): String? {
        return try {
            val config = loadedConfig ?: loadConfig()
            config.env[keyName]?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Logger.error("Failed to get API key for '$keyName': ${e.message}", throwable = e)
            null
        }
    }

    /**
     * Get all configured agent providers
     */
    fun getConfiguredProviders(): Set<String> {
        return try {
            val config = loadedConfig ?: loadConfig()
            config.agent.providers.keys
        } catch (e: Exception) {
            Logger.error("Failed to get configured providers: ${e.message}", throwable = e)
            emptySet()
        }
    }

    private fun getOperatingSystem(): OS {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("win") -> OS.WINDOWS
            osName.contains("mac") -> OS.MACOS
            osName.contains("nix") || osName.contains("nux") -> OS.LINUX
            else -> OS.UNKNOWN
        }
    }
}

/**
 * Operating System enum
 */
enum class OS {
    WINDOWS, MACOS, LINUX, UNKNOWN
}

/**
 * Application information data class
 */
data class AppInfo(
    val name: String,
    val version: String,
    val packageName: String,
    val configDir: String,
    val isFirstRun: Boolean
)

