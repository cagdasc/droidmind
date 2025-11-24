package com.cacaosd.droidmind.mind.verifier

import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy
import com.cacaosd.droidmind.mind.layout.model.flattenDfs
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.withContext

class UiTextVerifier(
    private val platformDispatchers: PlatformDispatchers,
) : Verifier {
    override suspend fun verify(
        optimisedHierarchy: OptimisedHierarchy,
        expectation: Expectation
    ): VerificationResult {
        val uiHierarchy = withContext(platformDispatchers.default) {
            optimisedHierarchy.flattenDfs { it.type.canHaveText() }
        }

        val result = withContext(platformDispatchers.default) {
            uiHierarchy.filter {
                it.text?.contains(expectation.value) ?: false
            }
        }
        val hasMatch = result.isNotEmpty()

        val message = if (hasMatch) {
            "UiText-Expectation met: UI contains '${expectation.value}'"
        } else {
            "UiText-Expectation not met: UI does not contain '${expectation.value}'"
        }
        return VerificationResult(passed = hasMatch, message = message)
    }
}
