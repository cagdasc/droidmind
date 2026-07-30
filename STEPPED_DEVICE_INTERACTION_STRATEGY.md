SteppedDeviceInteractionStrategy — Nodes, Inputs, Outputs, and Edge Conditions

This document visualizes the node graph, inputs/outputs, and edge conditions for SteppedDeviceInteractionStrategy.kt

```mermaid
flowchart TD
PromptClassification["PromptClassification\n- inScope: Boolean\n- requiresVerification: Boolean\n- request: String, reason: String"]
ProvisioningResult[ProvisioningResult\n- ready: Boolean\n- request: String\n- reason:String]
StrategyExecutionPlanV2[StrategyExecutionPlanV2\n -request: String\n - steps: List<StrategyStep>\n - summary: String]
InteractionResult[InteractionResult\n- request: String\n- summary: String]
  Start((nodeStart))
  classify["classifyRequest\nInput: String\nOutput: PromptClassification"]
  rewrite["rewritePrompt\nInput: PromptClassification\nOutput: String (raw plan JSON)"]
  persist["persistPlan\nInput: raw JSON\nOutput: String (request)"]
  plan_db[(executionPlanV2)]
  step_db[(currentStepIndex)]
  interaction_db[(lastInteraction)]
  identify["identifyDeviceAndApp\nInput: String\nOutput: ProvisioningResult"]
  applyStep["applyCurrentStep\nInput: ProvisioningResult\nOutput: ProvisioningResult"]
  interact["interactWithApp\nInput: ProvisioningResult\nOutput: InteractionResult"]
  store["storeLastInteraction\nInput: InteractionResult\nOutput: InteractionResult"]
  summarize["summarizeResult\nInput: InteractionResult\nOutput: CriticResult (feedback/summary)"]
  buildForVerify["buildInteractionForVerification\nInput: ProvisioningResult\nOutput: InteractionResult"]
  verify["verifyInteraction\nInput: InteractionResult\nOutput: CriticResult<InteractionResult> { successful, input, feedback }"]
  finish([nodeFinish])

  Start --> classify

  classify -- "!inScope" --> finish
  classify -- "inScope" --> rewrite --> persist --> identify
  persist --StrategyExecutionPlanV2--> plan_db
  persist --currentStepIndexKey--> step_db

  identify -- "!ready" --> finish
  identify -- "ready" --> applyStep

  plan_db --> applyStep
  step_db --> applyStep

  applyStep -- "isCurrentStepInteraction" --> interact
  applyStep -- "isCurrentStepVerification" --> buildForVerify

  interact --> store
  store --> interaction_db
  step_db --> store
  store --> step_db


  store -- "!hasMoreSteps" --> summarize
  summarize --successful--> finish
  summarize --!successful--> finish

  store -- "hasMoreSteps" --> applyStep

  buildForVerify --> verify

  verify -- "successful && !hasMoreSteps" --> summarize
  verify -- "successful && hasMoreSteps" --> store
  verify -- "!successful OR !hasMoreSteps" --> finish
```

Legend & Behavior Notes
- Plan formats: Supports StrategyExecutionPlanV2 (preferred): ordered steps with types INTERACTION or VERIFICATION. persistPlan stores the parsed StrategyExecutionPlanV2 into executionPlanV2 storage and initializes currentStepIndex = 0.
- Storage keys used:
  - executionPlanV2 (executionPlanV2Key)
  - currentStepIndex (currentStepIndexKey)
  - lastInteraction (lastInteractionKey)
  - verificationAttemptsKey
  - requiresVerificationKey
  - maxVerificationAttempts = 3
- applyCurrentStep behavior: reads executionPlanV2 and currentStepIndex from storage; when a step exists its content replaces the ProvisioningResult.request so downstream nodes act on the step.content.
- Interaction step flow: interactWithApp -> storeLastInteraction. storeLastInteraction persists lastInteraction and increments currentStepIndex. After storing, if hasMoreSteps -> applyCurrentStep (next step); otherwise -> summarizeResult -> finish.
- Verification step flow: buildInteractionForVerification -> verifyInteraction. On success:
  - if hasMoreSteps -> verify result is transformed into an InteractionResult (feedback used as summary) and forwarded to storeLastInteraction, advancing the plan; loop continues.
  - if no more steps -> summarizeResult -> finish (final summary returned).
- Failure path: when verifyInteraction reports unsuccessful, the current strategy finalizes and returns a failure/"gave up" message. The code contains handleVerificationFailure (backtracking to a previous INTERACTION) but edges connecting it are currently commented out.
- Edge evaluation and storage timing: storeLastInteraction increments currentStepIndex as a side-effect before branch checks; edge ordering matters for correct progression.

Quick node summary
- classifyRequest: scope gate (no tools). Produces PromptClassification (inScope, requiresVerification, request, reason).
- rewritePrompt: rewrites request into machine-readable plan JSON (no tools). persistPlan parses and stores the plan.
- identifyDeviceAndApp: prepares target device/app (DeviceManagerTools). Produces ProvisioningResult(ready, request, reason).
- applyCurrentStep: injects current step.content into the provisioning request.
- interactWithApp: executes INTERACTION steps using UiHierarchyTools + UiInteractionTools, returns InteractionResult(request, summary).
- buildInteractionForVerification: builds an InteractionResult for VERIFICATION steps (uses lastInteraction if present).
- verifyInteraction: built-in critic that validates UI state, returns CriticResult(successful, input, feedback).
- storeLastInteraction: persists lastInteraction JSON and advances currentStepIndex.
- summarizeResult: final summarizer (toolless critic) that returns a user-facing summary.

Looping & Retry
- Normal progression: persistPlan (currentStepIndex=0) -> applyCurrentStep -> (INTERACTION) interactWithApp -> storeLastInteraction (index++) -> applyCurrentStep -> ... until no steps remain, then summarizeResult -> finish.
- Verification steps consume no new interaction; they validate current UI using lastInteraction context. Successful verification either advances the plan or leads to final summarization. Unsuccessful verification currently ends the run with a failure message.
- Note: handleVerificationFailure is implemented to backtrack to the nearest prior INTERACTION and retry, but the strategy does not wire it into the active edges (commented-out). Re-enabling it would allow bounded retry behavior using verificationAttemptsKey and maxVerificationAttempts.
