// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.agent

/**
 * Cloud LLM provider and model definitions.
 * Used by LlmConfigActivity to render the provider tabs + model cards.
 */

data class CloudModel(
    val id: String,
    val displayName: String,
    val inputPricePerM: Double,
    val outputPricePerM: Double,
    val tier: ModelTier,
    val contextSize: Int,
    val recommended: Boolean = false,
    val description: String = ""
)

enum class ModelTier(val stars: String, val label: String) {
    LITE("☆", "Lite"),
    FAST("★", "Fast"),
    SMART("★★", "Smart"),
    PRO("★★★", "Pro")
}

enum class CloudProvider(
    val displayName: String,
    val defaultBaseUrl: String,
    val models: List<CloudModel>,
    val showBaseUrl: Boolean = false
) {
    OPENAI(
        displayName = "OpenAI",
        defaultBaseUrl = "https://api.openai.com/v1",
        models = listOf(
            CloudModel("gpt-4o-mini", "GPT-4o Mini", 0.15, 0.60, ModelTier.FAST, 128_000, recommended = true),
            CloudModel("gpt-4o", "GPT-4o", 2.50, 10.00, ModelTier.SMART, 128_000),
            CloudModel("gpt-4.1", "GPT-4.1", 2.00, 8.00, ModelTier.PRO, 1_000_000),
            CloudModel("gpt-4.1-mini", "GPT-4.1 Mini", 0.40, 1.60, ModelTier.FAST, 1_000_000),
            CloudModel("gpt-4.1-nano", "GPT-4.1 Nano", 0.10, 0.40, ModelTier.LITE, 1_000_000),
        )
    ),
    ANTHROPIC(
        displayName = "Anthropic",
        defaultBaseUrl = "https://api.anthropic.com/v1",
        models = listOf(
            CloudModel("claude-sonnet-4-6", "Claude Sonnet 4.6", 3.00, 15.00, ModelTier.PRO, 200_000),
            CloudModel("claude-haiku-4-5", "Claude Haiku 4.5", 0.80, 4.00, ModelTier.FAST, 200_000, recommended = true),
        )
    ),
    GOOGLE(
        displayName = "Google",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta",
        models = listOf(
            CloudModel("gemini-2.5-flash", "Gemini 2.5 Flash", 0.15, 0.60, ModelTier.FAST, 1_000_000, recommended = true),
            CloudModel("gemini-2.5-pro", "Gemini 2.5 Pro", 1.25, 10.00, ModelTier.PRO, 1_000_000),
        )
    ),
    NVIDIA(
        displayName = "NVIDIA NIM",
        defaultBaseUrl = "https://integrate.api.nvidia.com/v1",
        models = listOf(
            CloudModel("meta/llama-3.3-70b-instruct", "Llama 3.3 70B", 0.0, 0.0, ModelTier.SMART, 128_000, recommended = true, description = "Allrounder: Review, Doku, Zusammenfassen"),
            CloudModel("meta/llama-3.1-8b-instruct", "Llama 3.1 8B", 0.0, 0.0, ModelTier.LITE, 128_000, description = "Schnell: Klassifikation, Extraktion"),
            CloudModel("meta/llama-4-maverick-17b-128e-instruct", "Llama 4 Maverick", 0.0, 0.0, ModelTier.FAST, 128_000, description = "Coding & anspruchsvolle Tasks"),
            CloudModel("nvidia/llama-3.3-nemotron-super-49b-v1.5", "Nemotron Super 49B", 0.0, 0.0, ModelTier.SMART, 128_000, description = "Reasoning wenn 70B nicht reicht"),
            CloudModel("nvidia/nvidia-nemotron-nano-9b-v2", "Nemotron Nano 9B", 0.0, 0.0, ModelTier.LITE, 128_000, description = "Schnelles Reasoning, einfache Logik"),
            CloudModel("qwen/qwen3-next-80b-a3b-instruct", "Qwen3 Next 80B", 0.0, 0.0, ModelTier.SMART, 128_000, description = "Reasoning & Mathe"),
            CloudModel("meta/llama-3.2-90b-vision-instruct", "Llama 3.2 90B Vision", 0.0, 0.0, ModelTier.PRO, 128_000, description = "Bildanalyse / Vision"),
        )
    ),
    CUSTOM(
        displayName = "Custom",
        defaultBaseUrl = "",
        models = emptyList(),
        showBaseUrl = true
    );

    companion object {
        /**
         * Find provider by name (case-insensitive).
         * Returns OPENAI as default.
         */
        fun fromName(name: String): CloudProvider {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: OPENAI
        }

        /**
         * Find the provider that contains a given model ID.
         */
        fun findProviderForModel(modelId: String): CloudProvider? {
            return entries.find { provider ->
                provider.models.any { it.id == modelId }
            }
        }
    }
}
