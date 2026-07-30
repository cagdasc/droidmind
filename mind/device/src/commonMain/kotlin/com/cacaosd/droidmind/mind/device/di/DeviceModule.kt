package com.cacaosd.droidmind.mind.device.di

import com.cacaosd.droidmind.core.config.di.SelfResolveQualifier
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.droidmind.mind.device.controller.getIosDeviceController
import com.cacaosd.droidmind.mind.device.controller.provideAndroidDeviceController
import com.cacaosd.droidmind.mind.layout.di.AndroidLayoutParserQualifier
import com.cacaosd.droidmind.mind.layout.di.IosLayoutParserQualifier
import org.koin.dsl.bind
import org.koin.dsl.module

object AndroidDeviceManagerQualifier : SelfResolveQualifier()

object IosDeviceControllerQualifier : SelfResolveQualifier()

val deviceModule = module {
    includes(coreConfigModule)

    single(AndroidDeviceManagerQualifier) {
        provideAndroidDeviceController(
            appConfigManager = get(),
            layoutParser = get(AndroidLayoutParserQualifier),
            platformDispatchers = get(),
            clock = get()
        )
    } bind DeviceController::class

    single(IosDeviceControllerQualifier) {
        getIosDeviceController(
            json = get(),
            clock = get(),
            appConfigManager = get(),
            layoutParser = get(IosLayoutParserQualifier)
        )
    } bind DeviceController::class
}