// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.support

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import io.agents.pokeclaw.AppCapabilityCoordinator
import io.agents.pokeclaw.BuildConfig
import io.agents.pokeclaw.agent.llm.LocalBackendHealth
import io.agents.pokeclaw.agent.llm.ModelConfigRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DebugReportManager {

    private const val REPORT_DIR = "debug_reports"
    private const val LOGCAT_LINES = "400"
    private const val MAX_HTTP_LOGS = 5

    fun buildReport(context: Context): File {
        val reportDir = File(context.cacheDir, REPORT_DIR).apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val output = File(reportDir, "pokeclaw-debug-$timestamp.zip")

        ZipOutputStream(FileOutputStream(output)).use { zip ->
            addText(zip, "summary.txt", buildSummary(context))
            collectLogcat().takeIf { it.isNotBlank() }?.let { addText(zip, "app-logcat.txt", it) }
            addRecentHttpLogs(zip, context.cacheDir)
        }

        return output
    }

    private fun buildSummary(context: Context): String {
        val capabilities = AppCapabilityCoordinator.snapshot(context)
        val config = ModelConfigRepository.snapshot()
        val httpDir = File(context.cacheDir, "http_logs")
        val httpLogs = httpDir.listFiles()?.size ?: 0
        return buildString {
            appendLine("PokeClaw Debug Report")
            appendLine("Generated: ${Date()}")
            appendLine()
            appendLine("App")
            appendLine("- Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("- Debug build: ${BuildConfig.DEBUG}")
            appendLine("- Package: ${BuildConfig.APPLICATION_ID}")
            appendLine()
            appendLine("Device")
            appendLine("- Manufacturer: ${Build.MANUFACTURER}")
            appendLine("- Model: ${Build.MODEL}")
            appendLine("- Device: ${Build.DEVICE}")
            appendLine("- Hardware: ${Build.HARDWARE}")
            appendLine("- Fingerprint: ${Build.FINGERPRINT}")
            appendLine("- Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("- Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            appendLine("- RAM (total): ${getDeviceRamGb(context)} GB")
            appendLine()
            appendLine("Local Inference Runtime (#41 / #14 diagnostics)")
            val openClPaths = detectOpenClLibraryPaths()
            appendLine("- OpenCL libraries found: ${if (openClPaths.isEmpty()) "(none) — GPU path will not work" else openClPaths.joinToString(", ")}")
            appendLine("- Backend health: ${LocalBackendHealth.debugStateSummary()}")
            appendLine()
            appendLine("Capabilities")
            appendLine("- Accessibility: ${capabilities.accessibilityStatusLabel}")
            appendLine("- Notification access: ${capabilities.notificationAccessStatusLabel}")
            appendLine("- Notification permission: ${capabilities.notificationPermissionStatusLabel}")
            appendLine("- Overlay: ${if (capabilities.overlayGranted) "Enabled" else "Disabled"}")
            appendLine("- Battery optimization: ${if (capabilities.batteryOptimizationIgnored) "Unrestricted" else "Restricted"}")
            appendLine("- Foreground service: ${if (capabilities.foregroundServiceRunning) "Running" else "Stopped"}")
            appendLine()
            appendLine("LLM")
            appendLine("- Active mode: ${config.activeMode}")
            appendLine("- Active cloud model: ${config.activeCloud.modelName.ifBlank { "(none)" }}")
            appendLine("- Default local model: ${config.local.displayName.ifBlank { "(none)" }}")
            appendLine("- Local path: ${config.local.modelPath.ifBlank { "(none)" }}")
            appendLine("- Local backend preference: ${config.local.backendPreference.ifBlank { "(default)" }}")
            appendLine("- CPU-safe mode: ${if (LocalBackendHealth.isCpuSafeModeEnabled()) "Enabled" else "Disabled"}")
            appendLine("- CPU-safe reason: ${LocalBackendHealth.cpuSafeReason().ifBlank { "(none)" }}")
            appendLine()
            appendLine("Artifacts")
            appendLine("- HTTP log files present: $httpLogs")
        }
    }

    private fun collectLogcat(): String {
        return runCatching {
            val process = ProcessBuilder(
                "logcat",
                "-d",
                "-v",
                "threadtime",
                "-t",
                LOGCAT_LINES,
                "ClawA11yService:V",
                "ClawNotifListener:V",
                "AutoReplyManager:V",
                "ForegroundService:V",
                "LocalBackendHealth:V",
                "EngineHolder:V",
                "LocalModelRuntime:V",
                "InputTextTool:V",
                "SendMessageTool:V",
                "*:S",
            ).redirectErrorStream(true).start()
            process.inputStream.bufferedReader().use { it.readText() }
        }.getOrElse { "Failed to collect logcat: ${it.message}" }
    }

    private fun addRecentHttpLogs(zip: ZipOutputStream, cacheDir: File) {
        val httpDir = File(cacheDir, "http_logs")
        val files = httpDir.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.take(MAX_HTTP_LOGS)
            ?: return
        for (file in files) {
            addFile(zip, "http_logs/${file.name}", file)
        }
    }

    private fun addText(zip: ZipOutputStream, entryName: String, content: String) {
        zip.putNextEntry(ZipEntry(entryName))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun addFile(zip: ZipOutputStream, entryName: String, file: File) {
        if (!file.exists() || !file.isFile) return
        zip.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { input -> input.copyTo(zip) }
        zip.closeEntry()
    }

    /** Returns total device RAM in GB (rounded up). Used in debug summary for GPU/model RAM
     *  sizing diagnostics (#41 / #14 — OEM bug reporters need to know real RAM vs minRamGb). */
    private fun getDeviceRamGb(context: Context): Int {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            (info.totalMem / (1024L * 1024L * 1024L)).toInt() + 1
        } catch (_: Throwable) {
            -1
        }
    }

    /** Probes well-known Android OpenCL driver paths. If none exist the GPU LiteRT path will
     *  fail at first inference (this is the root cause for issue #14 and the Pixel-8-Pro
     *  GPU→CPU fallback documented in #41). Returning the actual paths found makes triage
     *  trivial for non-Pixel OEM reporters. */
    private fun detectOpenClLibraryPaths(): List<String> {
        val candidates = listOf(
            "/system/vendor/lib64/libOpenCL.so",
            "/system/vendor/lib/libOpenCL.so",
            "/vendor/lib64/libOpenCL.so",
            "/vendor/lib/libOpenCL.so",
            "/system/lib64/libOpenCL.so",
            "/system/lib/libOpenCL.so",
        )
        return candidates.filter {
            runCatching { File(it).exists() }.getOrDefault(false)
        }
    }
}
