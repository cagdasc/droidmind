package com.cacaosd.droidmind.core.config.models

import kotlinx.serialization.Serializable

/**
 * Root configuration structure for DroidMind
 */
@Serializable
data class AppConfig(
    val app: AppInfo = AppInfo(),
    val env: Map<String, String> = emptyMap(),
    val api: ApiConfig = ApiConfig(),
    val agent: AgentConfig = AgentConfig()
)

/**
 * Application metadata
 */
@Serializable
data class AppInfo(
    val name: String = "DroidMind",
    val version: String = "1.0"
)

/**
 * API keys configuration
 */
@Serializable
data class ApiConfig(
    val keys: Map<String, String> = emptyMap()
)

/**
 * Agent providers configuration
 */
@Serializable
data class AgentConfig(
    val providers: Map<String, AgentProviderConfig> = emptyMap()
)

/**
 * Individual agent provider configuration with defaults
 */
@Serializable
data class AgentProviderConfig(
    val maxIterations: Int = DEFAULT_MAX_ITERATIONS,
    val temperature: Double = DEFAULT_TEMPERATURE,
    val baseUrl: String = "",
) {
    companion object {
        const val DEFAULT_MAX_ITERATIONS = 250
        const val DEFAULT_TEMPERATURE = 0.2
    }
}
