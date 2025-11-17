package com.cacaosd.droidmind.mind.device.ios

import com.kgit2.kommand.process.Command
import com.kgit2.kommand.process.Stdio
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.ios.options.XCUITestOptions
import kotlinx.serialization.json.Json
import java.net.URI

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

    fun launchApp(udid: String, bundleId: String): Boolean {
        return Command("idb")
            .args(listOf("launch", "--udid", udid, bundleId))
            .stdout(Stdio.Null)
            .spawn()
            .wait() == 0
    }

    fun getDriver(udid: String): IOSDriver {
        val options = XCUITestOptions().apply { setUdid(udid) }
        return IOSDriver(URI("http://192.168.0.59:8100").toURL(), options)
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

    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, serial: String): Boolean {
        return Command("idb")
            .args(
                listOf(
                    "ui", "swipe",
                    startX.toString(), startY.toString(),
                    endX.toString(), endY.toString(),
                    "--udid", serial
                )
            )
            .stdout(Stdio.Null)
            .spawn()
            .wait() == 0
    }
}
