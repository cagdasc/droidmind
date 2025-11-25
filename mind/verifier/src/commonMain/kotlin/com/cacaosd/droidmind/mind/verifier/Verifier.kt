package com.cacaosd.droidmind.mind.verifier

import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy

interface Verifier {
    suspend fun verify(optimisedHierarchy: OptimisedHierarchy, expectation: Expectation): VerificationResult
}

sealed class Expectation(open val value: String) {
    data class Text(override val value: String) : Expectation(value)
}

data class VerificationResult(val passed: Boolean, val message: String)
