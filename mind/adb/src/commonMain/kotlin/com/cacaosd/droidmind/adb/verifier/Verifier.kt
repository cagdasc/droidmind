package com.cacaosd.droidmind.adb.verifier

interface Verifier {
    suspend fun verify(serial: String, packageName: String, expectation: Expectation): VerificationResult
}

data class Expectation(val type: String, val value: String)
data class VerificationResult(val passed: Boolean, val message: String)
