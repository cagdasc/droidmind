---
name: strategy-builder
description: A reusable skill for creating, managing, and extending Koog Agents strategies for the DroidMind framework.
---

## Overview

This skill helps you build multi-layer agent strategies using the Koog Agents DSL. It covers:
- Creating new strategy layers
- Defining typed result data classes
- Building execution flow graphs (nodes and edges)
- Implementing conditional routing
- Managing state and storage

## Core Concepts

### Strategy Structure

A strategy is a directed graph of nodes and edges:

```kotlin
fun createStrategy() = strategy<InputType, OutputType>("strategy_name") {
    // 1. Define nodes (computation steps)
    val step1 by subgraphWithTask<InputType, ResultType1>(...) { input -> ... }
    val step2 by subgraphWithTask<ResultType1, ResultType2>(...) { result -> ... }
    
    // 2. Define edges (routing between nodes)
    edge(nodeStart forwardTo step1)
    edge(step1 forwardTo step2)
    edge(step2 forwardTo nodeFinish transformed { ... })
}
```

### Key Components

| Component | Purpose | Example |
|-----------|---------|---------|
| **Node** | Computation step (LLM call or data transformation) | `subgraphWithTask`, `node` |
| **Edge** | Connection between nodes with optional conditions | `edge(nodeA forwardTo nodeB onCondition {...})` |
| **Storage** | Persistent state within a strategy run | `storage.set(key, value)` |
| **Data Class** | Typed result structure with `@LLMDescription` | `PromptClassification` |

---

## Creating a New Layer

### Step 1: Define Result Data Class

```kotlin
@Serializable
@LLMDescription("Brief description of what this layer outputs")
data class MyLayerResult(
    @property:LLMDescription("Field 1 description")
    val field1: String,
    
    @property:LLMDescription("Field 2 description - required for LLM to understand output")
    val field2: Boolean,
    
    @property:LLMDescription("Carries the original request forward")
    val request: String
)
```

**Pattern**: Always include:
- `@Serializable` - for framework serialization
- `@LLMDescription` on class - what this result represents
- `@property:LLMDescription` on each field - what each field means to the LLM

### Step 2: Create Storage Keys (if needed)

```kotlin
private val myLayerRetryKey = createStorageKey<Int>("my-layer-retries")
private val myLayerStateKey = createStorageKey<MyState>("my-layer-state")
```

### Step 3: Define the Node

```kotlin
val myLayer by subgraphWithTask<InputType, MyLayerResult>(
    name = "my_layer_name",
    tools = toolsToUse.asTools()  // Filter to only needed tools
) { input ->
    """
    Phase: Description of what this layer does.
    Keep prompts concise to reduce token usage.
    
    Input context: ${input.field}
    """.trimIndent()
}
```

**Pattern**: 
- `name`: Kebab-case, used for debugging/logging
- `tools`: Only include tools needed for this layer
- Prompt: Concise, includes context from input

### Step 4: Add Edges

```kotlin
// Success path
edge(
    myLayer forwardTo nextLayer
        onCondition { it.field1 == "success" }
)

// Failure path
edge(
    myLayer forwardTo nodeFinish
        onCondition { it.field1 != "success" }
        transformed { "Layer failed: ${it.field2}" }
)
```

**Pattern**:
- Conditions are evaluated in order defined
- Use `transformed` to format final output
- Early exits should come before retry/continue edges

---

## Common Patterns

### Pattern 1: Simple Sequential Flow

```kotlin
val step1 by subgraphWithTask<Input, Result1>(...)  { ... }
val step2 by subgraphWithTask<Result1, Result2>(...) { ... }

edge(nodeStart forwardTo step1)
edge(step1 forwardTo step2)
edge(step2 forwardTo nodeFinish transformed { "Completed: ${it.summary}" })
```

### Pattern 2: Conditional Branching

```kotlin
edge(
    classifier forwardTo finish
        onCondition { !it.approved }
        transformed { "Rejected: ${it.reason}" }
)

edge(
    classifier forwardTo processor
        onCondition { it.approved }
)
```

### Pattern 3: Retry Loop with Max Attempts

```kotlin
private val retryAttemptsKey = createStorageKey<Int>("retry-attempts")
private val maxAttempts = 3

val verifier by subgraphWithVerification<ResultType>(
    tools = verificationTools.asTools()
) { result -> ... }

// Max retries reached
edge(
    verifier forwardTo nodeFinish
        onCondition { 
            !it.successful && (storage.get(retryAttemptsKey) ?: 0) >= maxAttempts 
        }
        transformed { "Failed after $maxAttempts attempts" }
)

// Retry available
edge(
    verifier forwardTo retry
        onCondition { !it.successful }
)
```

### Pattern 4: State Transformation Node

For non-LLM transformations, use `node<InputType, OutputType>`:

```kotlin
val prepareRetry by node<CriticResult<ResultType>, InputType> { criticResult ->
    val attempts = (storage.get(retryAttemptsKey) ?: 0) + 1
    storage.set(retryAttemptsKey, attempts)
    
    InputType(
        request = criticResult.input.request,
        feedback = "Retry feedback: ${criticResult.feedback}"
    )
}
```

### Pattern 5: Carrying Context Forward

Always pass the original request through layers:

```kotlin
@Serializable
data class LayerResult(
    val request: String,  // ALWAYS include
    val result: String,
    val metadata: String = ""
)

// In each subsequent layer, reference via:
"""
Original request: ${input.request}
Previous result: ${input.result}
"""
```

---

## Storage and State Management

### Creating Storage Keys

```kotlin
private val keyName = createStorageKey<DataType>("readable-key-name")
```

**Types**: 
- `createStorageKey<Int>`, `<String>`, `<Boolean>`, `<CustomData>` etc.
- Custom data classes must be `@Serializable`

### Using Storage

```kotlin
// Set
storage.set(myKey, value)

// Get with default
val value = storage.get(myKey) ?: defaultValue

// Increment pattern
val count = (storage.get(countKey) ?: 0) + 1
storage.set(countKey, count)
```

---

## Tool Integration

### Filtering Tools by Layer

Only pass tools needed for each layer to reduce token usage:

```kotlin
// Layer that only reads
val reader by subgraphWithTask<Input, ReadResult>(
    tools = uiHierarchyTools.asTools()  // Only reading
) { ... }

// Layer that modifies
val modifier by subgraphWithTask<ReadResult, ModifyResult>(
    tools = uiInteractionTools.asTools()  // Only interaction
) { ... }

// Layer that needs all tools
val complex by subgraphWithTask<Input, ComplexResult>(
    tools = tool1.asTools() + tool2.asTools() + tool3.asTools()
) { ... }
```

### No Tools (Pure Classification)

```kotlin
val classifier by subgraphWithTask<String, Classification>(
    name = "classify",
    tools = emptyList()  // Pure judgment
) { request -> ... }
```

---

## Token Usage Best Practices

1. **Use Data Class Annotations**: Don't repeat field descriptions in prompts
   ```kotlin
   // Data class already explains the field
   @property:LLMDescription("Whether the action succeeded")
   val succeeded: Boolean
   
   // Prompt can reference it implicitly
   "Set succeeded = true if the element was tapped"
   ```

2. **Concise Prompts**: Every word costs tokens
   ```kotlin
   // ❌ Verbose
   "Please read the entire UI hierarchy of the application and locate all interactive elements"
   
   // ✅ Concise
   "Read the UI hierarchy and find interactive elements"
   ```

3. **Minimize Context Passing**: Use references instead of full text
   ```kotlin
   // ❌ Repeats full request
   val prepareRetry by node<...> { critic ->
       "${critic.input.request}\n\nFeedback: ${critic.feedback}"
   }
   
   // ✅ Compact reference
   val prepareRetry by node<...> { critic ->
       "Retry: ${critic.feedback}"
   }
   ```

4. **Tool Selection**: Only pass needed tools
   ```kotlin
   // ❌ All tools everywhere
   tools = allTools.asTools()
   
   // ✅ Layer-specific tools
   tools = uiHierarchyTools.asTools()  // This layer only reads
   ```

---

## Validation Checklist

Before deploying a strategy:

- ✅ All data classes have `@Serializable` and `@LLMDescription`
- ✅ All data class fields have `@property:LLMDescription`
- ✅ All edges lead to `nodeFinish`
- ✅ Retry loops have max attempt bounds
- ✅ Storage keys are defined before use
- ✅ Tool lists match available injected dependencies
- ✅ Prompts are concise (aim for <100 tokens per prompt)
- ✅ Conditions in edges are ordered (check constraints before generic conditions)
- ✅ Compiles: `./gradlew mind:agent:build`

---

## Example: Simple Classification → Execution → Verification Strategy

```kotlin
class MyCustomClient(
    private val classifierTools: ClassifierTools,
    private val executorTools: ExecutorTools,
    private val verifierTools: VerifierTools
) {
    fun createStrategy() = strategy<String, String>("my_workflow") {
        
        // Layer 0: Classify
        @Serializable
        data class Classification(
            val inScope: Boolean,
            val request: String,
            val category: String = ""
        )
        
        val classify by subgraphWithTask<String, Classification>(
            name = "classify",
            tools = emptyList()
        ) { request ->
            "Classify: $request"
        }
        
        val applyClass by node<Classification, String> { c ->
            storage.set(createStorageKey<String>("category"), c.category)
            c.request
        }
        
        // Layer 1: Execute
        data class ExecutionResult(
            val request: String,
            val result: String
        )
        
        val execute by subgraphWithTask<String, ExecutionResult>(
            name = "execute",
            tools = executorTools.asTools()
        ) { request ->
            "Execute: $request"
        }
        
        // Layer 2: Verify
        val verify by subgraphWithVerification<ExecutionResult>(
            tools = verifierTools.asTools()
        ) { result ->
            "Verify: ${result.result}"
        }
        
        // Routing
        edge(nodeStart forwardTo classify)
        edge(classify forwardTo nodeFinish onCondition { !it.inScope })
        edge(classify forwardTo applyClass onCondition { it.inScope })
        edge(applyClass forwardTo execute)
        edge(execute forwardTo verify)
        edge(verify forwardTo nodeFinish onCondition { it.successful })
        edge(verify forwardTo nodeFinish onCondition { !it.successful } 
            transformed { "Verification failed" })
    }
}
```

---

## File Location

Strategies go in `mind/agent/src/commonMain/kotlin/com/cacaosd/droidmind/agent/strategy/`

Name convention: `<Domain>InteractionStrategy.kt` or `<Domain>ClientStrategy.kt`

Register in `mind/agent/src/commonMain/kotlin/com/cacaosd/droidmind/agent/di/AgentModule.kt` as a Koin single.
