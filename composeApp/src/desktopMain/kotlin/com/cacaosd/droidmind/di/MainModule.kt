@file:OptIn(ExperimentalUuidApi::class)

package com.cacaosd.droidmind.di

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue
import org.koin.core.qualifier.TypeQualifier
import org.koin.dsl.module
import kotlin.uuid.ExperimentalUuidApi

internal object GoogleAgentQualifier : SelfResolveQualifier()

internal object MetaAgentQualifier : SelfResolveQualifier()

internal object CustomAgentQualifier : SelfResolveQualifier()

internal object AgentMessageFlowQualifier : SelfResolveQualifier()

internal object AndroidLayoutParserQualifier : SelfResolveQualifier()

internal object IosLayoutParserQualifier : SelfResolveQualifier()

internal object AndroidDeviceControllerQualifier : SelfResolveQualifier()

internal object IosDeviceControllerQualifier : SelfResolveQualifier()

internal abstract class SelfResolveQualifier : Qualifier {
    override val value: QualifierValue
        get() = TypeQualifier(this::class).value
}

val mainModule = module {
    includes(utilityModule, toolsModule, agentModule)
}
