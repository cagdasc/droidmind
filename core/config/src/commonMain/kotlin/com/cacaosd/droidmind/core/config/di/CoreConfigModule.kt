package com.cacaosd.droidmind.core.config.di

import com.cacaosd.droidmind.core.config.AppConfigManager
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue
import org.koin.core.qualifier.TypeQualifier
import org.koin.dsl.module

abstract class SelfResolveQualifier : Qualifier {
    override val value: QualifierValue
        get() = TypeQualifier(this::class).value
}

val json = Json {
    prettyPrint = true          // formatted output
    isLenient = false            // allow non-strict JSON
    ignoreUnknownKeys = true    // ignore fields not in your class
    encodeDefaults = false       // include default values in output
    explicitNulls = false
    coerceInputValues = true
}

val xml = XML {
    defaultPolicy {
        ignoreUnknownChildren()
    }
    autoPolymorphic = true
}

val coreConfigModule = module {
    single { json }
    single { xml }

    single {
        AppConfigManager(
            appName = "droidmind",
            appVersion = "0.0.1",
            packageName = "com.cacaosd.droidmind",
            clock = get()
        )
    }

    single { get<AppConfigManager>().localProperties }
}
