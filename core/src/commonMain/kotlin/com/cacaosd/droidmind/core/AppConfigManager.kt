package com.cacaosd.droidmind.core

import com.cacaosd.droidmind.core.logging.Logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Clock
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Manages application configuration directories and files
 * Following XDG Base Directory Specification and platform conventions
 */
class AppConfigManager(
    private val appName: String,
    private val appVersion: String = "1.0",
    private val packageName: String,
    private val clock: Clock
) {
    fun initializeApp() {
        // Initialize configuration
        // Check if first run
        if (initialize() && isFirstRun()) {
            Logger.debug("This is your first time running the app.")
            // Perform first-run setup
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
    val logsDir: Path = baseConfigDir.resolve("logs")

    // Configuration files
    val mainConfigFile: Path = configDir.resolve("config.properties")
    val userPrefsFile: Path = configDir.resolve("user-preferences.json")

    /**
     * Initialize all application directories and create default configuration files
     */
    private fun initialize(): Boolean {
        return try {
            createDirectoryStructure()
            createDefaultConfigFiles()
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
            logsDir
        )

        directories.forEach { dir ->
            try {
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir)
                    Logger.info("Created directory: ${dir.toAbsolutePath()}")
                }

                // Ensure directory is writable
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
     * Create default configuration files if they don't exist
     */
    private fun createDefaultConfigFiles() {
        // Main configuration file
        val dateTimeText = clock.instant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME)
        if (!Files.exists(mainConfigFile)) {
            val defaultConfig = """
                # $appName Configuration File
                # Version: $appVersion
                # Created: $dateTimeText
                
                app.name=${appName}
                app.version=${appVersion}
                app.first_run=true
                
                # Logging configuration
                log.level=INFO
                log.max_files=10
                log.max_size_mb=10
                
                # Performance settings
                cache.max_size_mb=100
                cache.cleanup_interval_hours=24
                
                # UI settings
                ui.theme=system
                ui.language=en
            """.trimIndent()

            Files.writeString(mainConfigFile, defaultConfig)
            Logger.info("Created default config file: ${mainConfigFile.fileName}")
        }

        // User preferences file
        if (!Files.exists(userPrefsFile)) {
            val defaultPrefs = """
                {
                  "window": {
                    "width": 800,
                    "height": 600,
                    "maximized": false,
                    "x": -1,
                    "y": -1
                  },
                  "recent_files": [],
                  "shortcuts": {},
                  "last_opened_directory": "${System.getProperty("user.home")}"
                }
            """.trimIndent()

            Files.writeString(userPrefsFile, defaultPrefs)
            Logger.info("Created default preferences file: ${userPrefsFile.fileName}")
        }
    }

    /**
     * Get file path within a specific directory
     */
    fun getConfigFile(filename: String): Path = configDir.resolve(filename)
    fun getUiDumpFile(filename: String): Path = uiDumpDir.resolve(filename)
    fun getScreenshotsFile(filename: String): Path = screenshotsDir.resolve(filename)
    fun getLogFile(filename: String): Path = logsDir.resolve(filename)

    /**
     * Check if this is the first run of the application
     */
    private fun isFirstRun(): Boolean {
        return try {
            if (!Files.exists(mainConfigFile)) return true

            val properties = java.util.Properties()
            Files.newInputStream(mainConfigFile).use { input ->
                properties.load(input)
            }

            properties.getProperty("app.first_run", "true").toBoolean()
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Mark first run as completed
     */
    private fun markFirstRunCompleted() {
        try {
            val properties = java.util.Properties()
            if (Files.exists(mainConfigFile)) {
                Files.newInputStream(mainConfigFile).use { input ->
                    properties.load(input)
                }
            }

            properties.setProperty("app.first_run", "false")

            Files.newOutputStream(mainConfigFile).use { output ->
                properties.store(output, "Updated first run status")
            }
        } catch (e: Exception) {
            Logger.error("Failed to update first run status: ${e.message}", e)
        }
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
