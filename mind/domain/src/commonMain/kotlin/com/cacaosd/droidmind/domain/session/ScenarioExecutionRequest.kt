package com.cacaosd.droidmind.domain.session

data class ScenarioExecutionRequest(
    val deviceSerial: String?,
    val packageName: String,
    val scenario: String,
    val executionMode: ExecutionMode,
    val expectation: String? = null
) {
    class Builder(
        private val deviceSerial: String?,
        private val packageName: String,
        private val scenario: String,
        private val executionMode: ExecutionMode
    ) {
        private var expectation: String? = null

        fun expectation(expectation: String?) = apply { this.expectation = expectation }

        fun build(): ScenarioExecutionRequest {
            val constructedScenario = when (executionMode) {
                ExecutionMode.TEXT -> {
                    """
                    Device serial is $deviceSerial
                    The application package name that will be launched is $packageName
                    The scenario to run: $scenario
                    """.trimIndent()
                }

                ExecutionMode.SCRIPT -> {
                    """
                        DEVICE serial $deviceSerial
                        APP package $packageName
                        $scenario
                    """.trimIndent()
                }
            }
            return ScenarioExecutionRequest(
                deviceSerial = deviceSerial,
                packageName = packageName,
                scenario = constructedScenario,
                executionMode = executionMode,
                expectation = expectation
            )
        }
    }

    companion object {
        fun builder(
            deviceSerial: String?,
            packageName: String,
            scenario: String,
            executionMode: ExecutionMode
        ) = Builder(deviceSerial, packageName, scenario, executionMode)
    }
}

enum class ExecutionMode {
    TEXT,
    SCRIPT
}
