package com.cacaosd.droidmind.core.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing

class JvmDispatcherProvider : DispatcherProvider {
    override val Main: CoroutineDispatcher
        get() = Dispatchers.Swing
    override val IO: CoroutineDispatcher
        get() = Dispatchers.IO
    override val Default: CoroutineDispatcher
        get() = Dispatchers.Default
    override val Unconfined: CoroutineDispatcher
        get() = Dispatchers.Unconfined
}
