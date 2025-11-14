package com.cacaosd.droidmind.mind.verifier

interface Verifier {
    suspend fun verify(serial: String, packageName: String, expectation: Expectation): VerificationResult
}

sealed class Expectation(open val value: String) {
    data class Text(override val value: String) : Expectation(value)
}

data class VerificationResult(val passed: Boolean, val message: String)
