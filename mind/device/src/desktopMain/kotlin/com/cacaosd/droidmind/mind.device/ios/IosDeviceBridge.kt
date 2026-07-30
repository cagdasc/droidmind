package com.cacaosd.droidmind.mind.device.ios

import com.kgit2.kommand.process.Command
import com.kgit2.kommand.process.Stdio
import io.appium.java_client.appmanagement.ApplicationState
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.ios.options.XCUITestOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import java.net.URI
import kotlin.time.Duration.Companion.milliseconds

class IosDeviceBridge(private val json: Json) {

    fun getDevices(): List<IdbDevice> {
        val child = Command("idb")
            .args(listOf("list-targets", "--json"))
            .stdout(Stdio.Pipe)
            .spawn()
        val devices = child.bufferedStdout()?.lines()?.map { line ->
            json.decodeFromString<IdbDevice>(line)
        }?.toList() ?: emptyList()
        child.wait()
        return devices
    }

    fun getInstalledApps(udid: String): List<IdbInstalledApp> {
        val child = Command("idb")
            .args(listOf("list-apps", "--udid", udid, "--json"))
            .stdout(Stdio.Pipe)
            .spawn()
        val apps = child.bufferedStdout()?.lines()?.map { line ->
            json.decodeFromString<IdbInstalledApp>(line)
        }?.toList() ?: emptyList()
        child.wait()
        return apps
    }

    fun launchApp(udid: String, bundleId: String) {
        useDriver(udid) {
            it.activateApp(bundleId)
        }
    }

    fun <T> useDriver(udid: String, block: (IOSDriver) -> T): T {
        val options = XCUITestOptions().apply { setUdid(udid) }
        val driver = IOSDriver(URI("http://192.168.0.59:4723").toURL(), options)

        try {
            return block(driver)
        } finally {
            driver.quit()
        }
    }

    fun sendInput(input: String, serial: String): Boolean {
        return Command("idb")
            .args(listOf("ui", "text", input, "--udid", serial))
            .stdout(Stdio.Null)
            .spawn()
            .wait() == 0
    }

    fun tap(x: Int, y: Int, serial: String): Boolean {
        return Command("idb")
            .args(listOf("ui", "tap", x.toString(), y.toString(), "--udid", serial))
            .stdout(Stdio.Null)
            .spawn()
            .wait() == 0
    }

    fun sendKeyEvent(key: Int, serial: String): Boolean {
        return Command("idb")
            .args(listOf("ui", "key", key.toString(), "--udid", serial))
            .stdout(Stdio.Null)
            .spawn()
            .wait() == 0
    }

    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Long, serial: String) {
        useDriver(serial) { driver ->
            driver.executeScript(
                "mobile: dragFromToForDuration",
                mapOf(
                    "duration" to durationMs / 1000.0,
                    "fromX" to startX,
                    "fromY" to startY,
                    "toX" to endX,
                    "toY" to endY
                )
            )
        }
    }

    suspend fun waitForAppToBeInForeground(serial: String, packageName: String) = withTimeoutOrNull(5000.milliseconds) {
        while (isActive) {
            delay(250.milliseconds)
            val appState = useDriver(serial) { driver ->
                driver.queryAppState(packageName)
            }
            if (appState == ApplicationState.RUNNING_IN_FOREGROUND) {
                return@withTimeoutOrNull true
            }
        }
        return@withTimeoutOrNull false
    } ?: false
}
