package com.cacaosd.droidmind.agent.strategy

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.environment.ReceivedToolResult
import ai.koog.agents.core.environment.result
import ai.koog.prompt.message.Message

fun reasoningStrategy(
    reasoningInterval: Int = 1,
    maxFeedbackAttempts: Int = 3,
    maxReasoningSteps: Int = 10,
    name: String = "re_act_chat"
): AIAgentGraphStrategy<String, String> = strategy(name) {
    require(reasoningInterval > 0) { "Reasoning interval must be greater than 0" }

    val reasoningStepKey = createStorageKey<Int>("reasoning_step")
    val feedbackAttemptKey = createStorageKey<Int>("feedback_attempt")

    val nodeSetup by node<String, String> {
        storage.set(reasoningStepKey, 0)
        storage.set(feedbackAttemptKey, 0)
        it
    }

    val nodeExecuteTool by nodeExecuteTool()
    val nodeCallLLM by node<Unit, Message.Response> {
        llm.writeSession { requestLLM() }
    }

    val giveFeedbackToCallTools by node<String, Message.Response> {
        val attempt = storage.getValue(feedbackAttemptKey)

        if (attempt >= maxFeedbackAttempts) {
//            error("Agent stuck in feedback loop after $maxFeedbackAttempts attempts without calling a tool.")
            llm.writeSession {
                appendPrompt {
                    user(
                        content = "You tried $attempt times to call a tool but failed each time. Call ExitTool and end conversation."
                    )
                }
            }
        }

        storage.set(feedbackAttemptKey, attempt + 1)

        llm.writeSession {
            appendPrompt {
                user(
                    content = "New you are in feedback stage since you didn't call a tool to execute next step. " +
                            "You must call a tool. Figure out the step that needs to be executed and find proper tool to do it."
                )
            }
            requestLLM()
        }
    }

    val nodeCallLLMReasonInput by node<String, Unit> { stageInput ->
        llm.writeSession {
            val toolNames = tools.joinToString(", ") { it.name }
            appendPrompt {
                user(stageInput)
                user(
                    "You received the user input above. " +
                            "Break down the task by using tools from [$toolNames]. " +
                            "Think step by step and figure out which tools you need to use regarding to tasks that you have created. " +
                            "Also evaluate result of the latest tool call and do the next operation."

                )
            }
            requestLLMWithoutTools()
        }
    }

    val nodeCallLLMReason by node<ReceivedToolResult, Unit> { result ->
        val reasoningStep = storage.getValue(reasoningStepKey)

        // Reset feedback counter — a tool was just called successfully
        storage.set(feedbackAttemptKey, 0)

//        if (reasoningStep >= maxReasoningSteps) {
//            error("Agent exceeded maximum reasoning steps ($maxReasoningSteps).")
//        }

        llm.writeSession {
            appendPrompt {
                tool { result(result) }
            }

            if (reasoningStep % reasoningInterval == 0) {
                val remaining = maxReasoningSteps - reasoningStep
                appendPrompt {
                    user(
                        "You received the tool result. " +
                                "Now have a look at the result of your last action and figure out next action" +
                                "If you cannot verify what the user was requested, finish the conversation."
                    )
                }
                requestLLMWithoutTools()
            }
        }

        storage.set(reasoningStepKey, reasoningStep + 1)
    }

    // ── Edges ─────────────────────────────────────────────────────────────────

    edge(nodeStart forwardTo nodeSetup)
    edge(nodeSetup forwardTo nodeCallLLMReasonInput)
    edge(nodeCallLLMReasonInput forwardTo nodeCallLLM)

    edge(nodeCallLLM forwardTo nodeExecuteTool onToolCall { true })
    edge(nodeCallLLM forwardTo giveFeedbackToCallTools onAssistantMessage { true })

    // giveFeedbackToCallTools mirrors nodeCallLLM edges;
    // the loop-vs-exit guard lives inside the node itself (throws on exceeded attempts)
    edge(giveFeedbackToCallTools forwardTo nodeExecuteTool onToolCall { true })
    edge(giveFeedbackToCallTools forwardTo giveFeedbackToCallTools onAssistantMessage { true })

    // After tool execution → reason → back to LLM action call
    edge(nodeExecuteTool forwardTo nodeCallLLMReason)
    edge(nodeCallLLMReason forwardTo nodeCallLLM)

    // nodeCallLLM: route based purely on response type — no storage needed here
    edge(nodeCallLLM forwardTo nodeFinish onAssistantMessage { true })
    edge(nodeCallLLM forwardTo nodeFinish onToolCall { tc -> tc.tool == "__exit__" } transformed { "Chat finished" })
//    edge(nodeCallLLM forwardTo nodeExecuteTool onToolCall { true })

}