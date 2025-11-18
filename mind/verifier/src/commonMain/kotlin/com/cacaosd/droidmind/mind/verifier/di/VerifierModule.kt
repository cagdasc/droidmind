package com.cacaosd.droidmind.mind.verifier.di

import com.cacaosd.droidmind.mind.verifier.UiTextVerifier
import com.cacaosd.droidmind.mind.verifier.Verifier
import org.koin.dsl.bind
import org.koin.dsl.module

val verifierModule = module {
    single {
        UiTextVerifier(platformDispatchers = get())
    } bind Verifier::class
}
