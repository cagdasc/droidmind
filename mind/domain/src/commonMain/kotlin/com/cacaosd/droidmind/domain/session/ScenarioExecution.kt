package com.cacaosd.droidmind.domain.session

data class ScenarioExecution(
    val deviceSerial: String?,
    val packageName: String,
    val scenario: String,
    val expectation: String? = null
) {
    class Builder {
        private var deviceSerial: String? = null
        private var packageName: String? = null
        private var scenario: String? = null
        private var expectation: String? = null

        fun deviceSerial(serial: String?) = apply { this.deviceSerial = serial }
        fun packageName(packageName: String?) = apply { this.packageName = packageName }
        fun scenario(scenario: String) = apply { this.scenario = scenario }
        fun expectation(expectation: String?) = apply { this.expectation = expectation }

        fun build(): ScenarioExecution {
            val serial = deviceSerial ?: throw IllegalStateException("deviceSerial is required")
            val pkg = packageName ?: throw IllegalStateException("packageName is required")
            val sc = scenario ?: throw IllegalStateException("scenario is required")
            return ScenarioExecution(
                deviceSerial = deviceSerial,
                packageName = pkg,
                scenario = """
                    Device serial is $serial
                    The application package name that will be launched is $pkg
                    The scenario to run: $sc
                    """,
                expectation = expectation
            )
        }
    }

    companion object {
        fun builder() = Builder()
    }
}
