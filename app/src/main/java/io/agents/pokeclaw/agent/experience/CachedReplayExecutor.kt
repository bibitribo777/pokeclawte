// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.agent.experience

import io.agents.pokeclaw.tool.ToolRegistry
import io.agents.pokeclaw.utils.XLog
import org.json.JSONObject

/**
 * Result of a cached trajectory replay.
 */
data class CachedReplayResult(
    val success: Boolean,
    val stepsUsed: Int,
    val message: String = ""
)

/**
 * Replays a sequence of cached tool calls deterministically.
 *
 * Observation-only tools (screen info, screenshots, etc.) are skipped.
 * Critical action tools that fail cause the replay to abort.
 */
class CachedReplayExecutor {

    companion object {
        private const val TAG = "CachedReplayExecutor"

        /** Tools whose failure should abort the replay. */
        private val CRITICAL_TOOLS = setOf(
            "tap", "tap_node", "swipe", "input_text", "open_app",
            "system_key", "long_press", "send_message", "make_call",
            "scroll_to_find", "find_and_tap"
        )
    }

    /**
     * Execute a cached tool call sequence.
     *
     * @param toolCalls the full trajectory (including observation tools)
     * @param onProgress optional progress callback (step, total, toolDisplayName)
     * @return result indicating success/failure and steps used
     */
    fun execute(
        toolCalls: List<CachedToolCall>,
        onProgress: ((step: Int, total: Int, description: String) -> Unit)? = null
    ): CachedReplayResult {
        val registry = ToolRegistry.getInstance()
        val actionCalls = toolCalls.filter {
            it.toolName !in ExperienceCache.OBSERVATION_TOOLS
        }
        val total = actionCalls.size
        val skipped = toolCalls.size - total

        XLog.i(TAG, "Replaying $total action tools (skipped $skipped observation)")

        var executed = 0
        for ((index, tc) in actionCalls.withIndex()) {
            val stepNum = index + 1
            val displayName = registry.getDisplayName(tc.toolName)
            onProgress?.invoke(stepNum, total, displayName)

            val params = try {
                val json = JSONObject(tc.parameters)
                val map = mutableMapOf<String, Any>()
                for (key in json.keys()) map[key] = json.get(key)
                map
            } catch (e: Exception) {
                XLog.w(TAG, "Bad params for ${tc.toolName}: ${tc.parameters}")
                emptyMap<String, Any>()
            }

            if (tc.toolName == "finish") {
                XLog.i(TAG, "Replay done at step $stepNum/$total")
                return CachedReplayResult(
                    success = true,
                    stepsUsed = executed,
                    message = params["message"] as? String ?: "Done (from cache)"
                )
            }

            try {
                val result = registry.executeTool(tc.toolName, params)
                executed++
                if (!result.isSuccess && tc.toolName in CRITICAL_TOOLS) {
                    XLog.w(TAG, "Critical tool ${tc.toolName} failed at step $stepNum: ${result.error}")
                    return CachedReplayResult(
                        success = false,
                        stepsUsed = executed,
                        message = "Step $stepNum: ${tc.toolName} failed — ${result.error ?: "unknown"}"
                    )
                }
            } catch (e: Exception) {
                XLog.e(TAG, "Tool ${tc.toolName} crashed at step $stepNum", e)
                if (tc.toolName in CRITICAL_TOOLS) {
                    return CachedReplayResult(
                        success = false,
                        stepsUsed = executed,
                        message = "Step $stepNum: ${tc.toolName} crashed — ${e.message ?: "unknown"}"
                    )
                }
            }
        }

        XLog.i(TAG, "Replay finished: $executed steps")
        return CachedReplayResult(
            success = true,
            stepsUsed = executed,
            message = "Done (replayed $executed cached steps)"
        )
    }
}
