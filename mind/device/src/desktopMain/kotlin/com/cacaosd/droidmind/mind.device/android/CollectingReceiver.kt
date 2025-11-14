package com.cacaosd.droidmind.mind.device.android

import com.android.ddmlib.IShellOutputReceiver

class CollectingReceiver : IShellOutputReceiver {
    private val builder = StringBuilder()
    override fun addOutput(data: ByteArray, offset: Int, length: Int) {
        builder.append(String(data, offset, length))
    }

    override fun flush() {}
    override fun isCancelled(): Boolean = false

    val result: String get() = builder.toString().trim()
    val resultLines: List<String> get() = result.lines()
}