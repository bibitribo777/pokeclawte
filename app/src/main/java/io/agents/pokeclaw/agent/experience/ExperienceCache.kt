// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.agent.experience

import io.agents.pokeclaw.ClawApplication
import io.agents.pokeclaw.utils.XLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * A single cached tool call in a trajectory.
 * @param toolName the tool name as used by ToolRegistry
 * @param parameters JSON string of the tool call parameters
 */
data class CachedToolCall(
    val toolName: String,
    val parameters: String
)

/**
 * A cached task trajectory entry.
 */
data class ExperienceEntry(
    val task: String,
    val toolCalls: List<CachedToolCall>,
    val createdAt: Long = System.currentTimeMillis(),
    var hitCount: Int = 0
)

/**
 * Experience replay cache — stores successful task trajectories and
 * matches new tasks against them to skip expensive LLM inference.
 *
 * Persisted to disk as JSON in the app's internal files directory.
 *
 * Matching strategy (v1): exact match on normalized task text
 * (trim, lowercase, collapse whitespace).
 */
object ExperienceCache {

    private const val TAG = "ExperienceCache"
    private const val CACHE_FILE = "experience_cache.json"
    private const val MAX_ENTRIES = 100

    /** Tools that only observe state and can be skipped during replay. */
    val OBSERVATION_TOOLS = setOf(
        "get_screen_info",
        "take_screenshot",
        "find_node_info",
        "get_installed_apps",
        "get_device_info",
        "get_notifications"
    )

    private val cache = LinkedHashMap<String, ExperienceEntry>()
    private var loaded = false

    // ── Persistence ──────────────────────────────────────────────────────────

    private fun cacheFile(): File {
        val dir = File(ClawApplication.instance.filesDir, "experience")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, CACHE_FILE)
    }

    @Synchronized
    private fun ensureLoaded() {
        if (loaded) return
        try {
            val file = cacheFile()
            if (file.exists()) {
                val json = JSONObject(file.readText())
                val entries = json.getJSONArray("entries")
                for (i in 0 until entries.length()) {
                    val entry = entries.getJSONObject(i)
                    val task = entry.getString("task")
                    val toolCallsJson = entry.getJSONArray("toolCalls")
                    val toolCalls = mutableListOf<CachedToolCall>()
                    for (j in 0 until toolCallsJson.length()) {
                        val tc = toolCallsJson.getJSONObject(j)
                        toolCalls.add(
                            CachedToolCall(
                                toolName = tc.getString("toolName"),
                                parameters = tc.optString("parameters", "{}")
                            )
                        )
                    }
                    cache[task] = ExperienceEntry(
                        task = task,
                        toolCalls = toolCalls,
                        createdAt = entry.optLong("createdAt", System.currentTimeMillis()),
                        hitCount = entry.optInt("hitCount", 0)
                    )
                }
                XLog.i(TAG, "Loaded ${cache.size} cached trajectories")
            }
        } catch (e: Exception) {
            XLog.e(TAG, "Failed to load experience cache", e)
        }
        loaded = true
    }

    @Synchronized
    private fun save() {
        try {
            val json = JSONObject()
            val entriesJson = JSONArray()
            for ((_, entry) in cache) {
                val entryJson = JSONObject()
                entryJson.put("task", entry.task)
                val toolCallsJson = JSONArray()
                for (tc in entry.toolCalls) {
                    val tcJson = JSONObject()
                    tcJson.put("toolName", tc.toolName)
                    tcJson.put("parameters", tc.parameters)
                    toolCallsJson.put(tcJson)
                }
                entryJson.put("toolCalls", toolCallsJson)
                entryJson.put("createdAt", entry.createdAt)
                entryJson.put("hitCount", entry.hitCount)
                entriesJson.put(entryJson)
            }
            json.put("entries", entriesJson)
            cacheFile().writeText(json.toString(2))
            XLog.d(TAG, "Saved ${cache.size} entries")
        } catch (e: Exception) {
            XLog.e(TAG, "Failed to save experience cache", e)
        }
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Normalize a task string for cache lookups.
     */
    fun normalize(task: String): String {
        return task.trim().lowercase().replace(Regex("\\s+"), " ")
    }

    /**
     * Look up a cached trajectory. Returns the entry if found, null otherwise.
     * Increments the hit count on match.
     */
    @Synchronized
    fun get(task: String): ExperienceEntry? {
        ensureLoaded()
        val key = normalize(task)
        val entry = cache[key]
        if (entry != null) {
            entry.hitCount++
            XLog.i(TAG, "Cache hit: \"$key\" → ${entry.toolCalls.size} calls (hit #${entry.hitCount})")
        }
        return entry
    }

    /**
     * Store a trajectory. Skips empty or observation-only trajectories.
     * If an entry with the same key exists, it is updated.
     * Oldest entries are evicted when over [MAX_ENTRIES].
     */
    @Synchronized
    fun put(task: String, toolCalls: List<CachedToolCall>) {
        ensureLoaded()
        val key = normalize(task)
        if (toolCalls.isEmpty()) return
        val actionCalls = toolCalls.filter {
            it.toolName !in OBSERVATION_TOOLS && it.toolName != "finish"
        }
        if (actionCalls.isEmpty()) return

        val existing = cache[key]
        val entry = ExperienceEntry(
            task = key,
            toolCalls = toolCalls,
            createdAt = System.currentTimeMillis(),
            hitCount = existing?.hitCount ?: 0
        )
        cache[key] = entry
        XLog.i(TAG, "${if (existing != null) "Updated" else "New"} cache entry: \"$key\" (${toolCalls.size} calls)")

        while (cache.size > MAX_ENTRIES) {
            val oldest = cache.entries.first()
            cache.remove(oldest.key)
            XLog.d(TAG, "Evicted oldest: \"${oldest.key}\"")
        }
        save()
    }

    /**
     * Fuzzy-match a task against cached entries using Jaccard similarity.
     * Only checked when exact match fails. Returns the best match above
     * the threshold, or null.
     *
     * Jaccard = |words(A) ∩ words(B)| / |words(A) ∪ words(B)|
     *
     * @param task the user's task text
     * @param threshold minimum Jaccard score (0.0–1.0), default 0.6
     * @return best-matching entry or null
     */
    @Synchronized
    fun findSimilar(task: String, threshold: Double = 0.6): ExperienceEntry? {
        ensureLoaded()
        if (cache.isEmpty()) return null
        val words = normalize(task).split(" ").toSet()
        if (words.size < 3) return null // too short for reliable fuzzy match

        var bestEntry: ExperienceEntry? = null
        var bestScore = threshold

        for ((_, entry) in cache) {
            val entryWords = entry.task.split(" ").toSet()
            val intersection = words.intersect(entryWords).size.toDouble()
            val union = words.union(entryWords).size.toDouble()
            val score = intersection / union
            if (score > bestScore) {
                bestScore = score
                bestEntry = entry
            }
        }

        if (bestEntry != null) {
            bestEntry.hitCount++
            XLog.i(TAG, "Fuzzy cache hit: \"$task\" → \"${bestEntry.task}\" (Jaccard=${java.lang.String.format("%.2f", bestScore)})")
        }
        return bestEntry
    }

    /**
     * Remove a cached trajectory (e.g. when replay fails).
     */
    @Synchronized
    fun remove(task: String) {
        ensureLoaded()
        val key = normalize(task)
        if (cache.remove(key) != null) {
            XLog.i(TAG, "Removed: \"$key\"")
            save()
        }
    }

    @Synchronized
    fun size(): Int {
        ensureLoaded()
        return cache.size
    }

    @Synchronized
    fun clear() {
        cache.clear()
        save()
        XLog.i(TAG, "Cache cleared")
    }
}
