package com.cacaosd.droidmind.agent.client

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.agent.singleRunStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.PromptBuilder
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import kotlinx.datetime.Clock

/**
 * A builder for constructing an [AIAgent] with a fluent API.
 *
 * This builder simplifies the creation of an AIAgent by providing
 * chainable methods for configuration, making the setup process
 * more readable and maintainable.
 *
 * @param llmModel The language model to be used by the agent.
 * @param executor The prompt executor for handling LLM interactions.
 */
class AgentClientBuilder private constructor(
    private val llmModel: LLModel,
    private val executor: PromptExecutor
) {
    private var systemPrompt: String? = null
    private var additionalPrompts: MutableList<PromptBuilder.() -> Unit> = mutableListOf()
    private var toolRegistry: ToolRegistry = ToolRegistry.EMPTY
    private var strategy: AIAgentGraphStrategy<String, String> = singleRunStrategy()
    private var features: GraphAIAgent.FeatureContext.() -> Unit = {}
    private var maxIterations: Int = DEFAULT_MAX_ITERATIONS
    private var temperature: Double = DEFAULT_TEMPERATURE

    companion object {
        private const val DEFAULT_MAX_ITERATIONS = 50
        private const val DEFAULT_TEMPERATURE = 1.0

        /**
         * Creates a new builder instance.
         *
         * @param llmModel The language model to be used by the agent.
         * @param executor The prompt executor for handling LLM interactions.
         * @return A new builder instance.
         */
        fun create(llmModel: LLModel, executor: PromptExecutor): AgentClientBuilder {
            return AgentClientBuilder(llmModel, executor)
        }
    }

    /**
     * Sets the system prompt for the agent. This is a mandatory step.
     *
     * @param prompt The initial system prompt that defines the agent's role and behavior.
     * @return The builder instance for chaining.
     * @throws IllegalArgumentException if the prompt is blank.
     */
    fun withSystemPrompt(prompt: String): AgentClientBuilder {
        require(prompt.isNotBlank()) { "System prompt cannot be blank" }
        this.systemPrompt = prompt
        return this
    }

    /**
     * Sets the [ToolRegistry] for the agent.
     *
     * @param toolRegistry The collection of tools the agent can use.
     * @return The builder instance for chaining.
     */
    fun withTools(toolRegistry: ToolRegistry): AgentClientBuilder {
        this.toolRegistry = toolRegistry
        return this
    }

    /**
     * Sets the execution [AIAgentStrategy] for the agent.
     *
     * @param strategy The strategy defining how the agent will run (e.g., single run, loop).
     * @return The builder instance for chaining.
     */
    fun withStrategy(strategy: AIAgentGraphStrategy<String, String>): AgentClientBuilder {
        this.strategy = strategy
        return this
    }

    /**
     * Sets the maximum number of agent iterations.
     *
     * @param maxIterations The maximum number of iterations (must be positive).
     * @return The builder instance for chaining.
     * @throws IllegalArgumentException if maxIterations is not positive.
     */
    fun withMaxIterations(maxIterations: Int): AgentClientBuilder {
        require(maxIterations > 0) { "Max iterations must be positive, got: $maxIterations" }
        this.maxIterations = maxIterations
        return this
    }

    /**
     * Sets the temperature parameter for the language model.
     *
     * @param temperature The temperature value (must be between 0.0 and 2.0).
     * @return The builder instance for chaining.
     * @throws IllegalArgumentException if temperature is out of valid range.
     */
    fun withTemperature(temperature: Double): AgentClientBuilder {
        require(temperature in 0.0..2.0) { "Temperature must be between 0.0 and 2.0, got: $temperature" }
        this.temperature = temperature
        return this
    }

    /**
     * Adds a prompt configuration after the system prompt.
     * This allows for complex prompt setups with multiple user messages, examples, etc.
     *
     * @param promptConfig A lambda with [PromptBuilder] as its receiver to configure additional prompts.
     * @return The builder instance for chaining.
     */
    fun addPrompt(promptConfig: PromptBuilder.() -> Unit): AgentClientBuilder {
        additionalPrompts.add(promptConfig)
        return this
    }

    /**
     * Adds a user message to the prompt chain.
     * Convenience method for common use case of adding user messages.
     *
     * @param message The user message to add.
     * @return The builder instance for chaining.
     */
    fun addUserMessage(message: String): AgentClientBuilder {
        require(message.isNotBlank()) { "User message cannot be blank" }
        return addPrompt { user(message) }
    }

    /**
     * Adds an assistant message to the prompt chain.
     * Useful for providing examples or setting conversation context.
     *
     * @param message The assistant message to add.
     * @return The builder instance for chaining.
     */
    fun addAssistantMessage(message: String): AgentClientBuilder {
        require(message.isNotBlank()) { "Assistant message cannot be blank" }
        return addPrompt { assistant(message) }
    }

    /**
     * Adds multiple prompts at once using a configuration block.
     *
     * @param block A lambda that receives the builder instance for configuring multiple prompts.
     * @return The builder instance for chaining.
     */
    fun withPrompts(block: AgentClientBuilder.() -> Unit): AgentClientBuilder {
        this.block()
        return this
    }

    /**
     * @param block A lambda with [FeatureContext] as its receiver to configure features.
     * @return The builder instance for chaining.
     */
    fun withFeatures(block: GraphAIAgent.FeatureContext.() -> Unit): AgentClientBuilder {
        this.features = block
        return this
    }

    /**
     * Constructs and returns the configured [AIAgent].
     *
     * @return The final, configured AIAgent instance.
     * @throws IllegalStateException if the system prompt has not been set.
     */
    fun build(): AIAgent<String, String> {
        val finalSystemPrompt = systemPrompt
            ?: throw IllegalStateException("System prompt must be set before building the agent")

        return AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = AIAgentConfig(
                prompt = prompt(
                    "chat",
                    params = LLMParams(temperature = temperature, toolChoice = LLMParams.ToolChoice.Auto),
                    clock = Clock.System,
                ) {
                    system(finalSystemPrompt)
                    additionalPrompts.forEach { it() }
                },
                model = llmModel,
                maxAgentIterations = maxIterations,
            ),
            toolRegistry = toolRegistry,
            installFeatures = features
        )
    }
}
