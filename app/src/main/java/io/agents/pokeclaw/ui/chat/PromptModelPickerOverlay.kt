// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.agents.pokeclaw.agent.CloudProvider
import io.agents.pokeclaw.agent.llm.ModelConfigRepository
import io.agents.pokeclaw.utils.KVUtils
import java.io.File

/**
 * Compact per-prompt LLM picker shown next to the chat input.
 *
 * This does not create a new provider system; it reuses the existing
 * ModelConfigRepository + CloudProvider registry so every selection is the
 * same active model used by chat and task execution.
 */
@Composable
fun PromptModelPickerOverlay(
    isLocalModel: Boolean,
    onModelSwitch: (modelId: String, displayName: String) -> Unit,
    colors: PokeclawColors,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val snapshot = ModelConfigRepository.snapshot()
    val label = if (isLocalModel) {
        val localName = snapshot.local.displayName.ifBlank {
            KVUtils.getLocalModelPath().substringAfterLast('/').substringBeforeLast('.')
        }.ifBlank { "Local" }
        "LLM · $localName"
    } else {
        val provider = snapshot.activeCloud.provider.displayName
        val model = snapshot.activeCloud.modelName.ifBlank { "Cloud" }
        "LLM · $provider · $model"
    }

    Column(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.surface.copy(alpha = 0.96f),
            modifier = Modifier
                .border(0.5.dp, colors.inputBorder, RoundedCornerShape(14.dp))
                .clickable { expanded = true },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Choose model",
                    tint = colors.textTertiary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface),
        ) {
            val localPath = KVUtils.getLocalModelPath()
            if (localPath.isNotBlank() && File(localPath).exists()) {
                val localDisplay = snapshot.local.displayName.ifBlank {
                    File(localPath).nameWithoutExtension.replace("-", " ").replace("_", " ")
                }
                DropdownMenuItem(
                    text = {
                        ModelMenuText(
                            title = "Local · $localDisplay",
                            active = isLocalModel,
                            colors = colors,
                        )
                    },
                    onClick = {
                        expanded = false
                        onModelSwitch("LOCAL", localDisplay)
                    },
                )
                HorizontalDivider(color = colors.inputBorder)
            }

            var shownAnyCloud = false
            CloudProvider.entries
                .filter { it != CloudProvider.CUSTOM }
                .forEach { provider ->
                    val apiKey = KVUtils.getApiKeyForProvider(provider.name)
                    if (apiKey.isBlank()) return@forEach
                    shownAnyCloud = true
                    provider.models.forEach { model ->
                        val active = !isLocalModel && snapshot.activeCloud.providerName == provider.name && snapshot.activeCloud.modelName == model.id
                        DropdownMenuItem(
                            text = {
                                ModelMenuText(
                                    title = "${provider.displayName} · ${model.displayName}",
                                    active = active,
                                    colors = colors,
                                )
                            },
                            onClick = {
                                expanded = false
                                onModelSwitch(model.id, model.displayName)
                            },
                        )
                    }
                }

            if (!shownAnyCloud && localPath.isBlank()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "No model configured — open Models first",
                            fontSize = 12.sp,
                            color = colors.textTertiary,
                        )
                    },
                    onClick = { expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ModelMenuText(
    title: String,
    active: Boolean,
    colors: PokeclawColors,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) colors.accent else colors.textSecondary,
        )
        if (active) {
            Spacer(Modifier.width(6.dp))
            Text("✓", fontSize = 12.sp, color = colors.accent)
        }
    }
}
