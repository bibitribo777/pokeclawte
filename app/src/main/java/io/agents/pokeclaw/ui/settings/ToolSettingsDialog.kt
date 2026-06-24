// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.ui.settings

import android.content.Context
import android.widget.Toast
import io.agents.pokeclaw.tool.ToolRegistry

/**
 * Real Settings -> Tools -> Manage Tools implementation.
 *
 * This keeps the first version intentionally simple:
 * - shows every registered tool
 * - lets the user enable/disable non-core tools
 * - persists the disabled set through ToolRegistry/KVUtils
 * - hides disabled tools from LangChain4j tool specifications
 */
object ToolSettingsDialog {

    fun summary(): String {
        ensureMobileToolsRegistered()
        return "${ToolRegistry.getEnabledToolCount()} / ${ToolRegistry.getTotalToolCount()} enabled"
    }

    fun show(context: Context, onChanged: () -> Unit = {}) {
        ensureMobileToolsRegistered()

        val tools = ToolRegistry.getRegisteredTools()
            .sortedWith(compareBy({ !ToolRegistry.isMandatoryTool(it.getName()) }, { it.getDisplayName().lowercase() }))

        if (tools.isEmpty()) {
            Toast.makeText(context, "No tools registered yet", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = tools.map { tool ->
            val name = tool.getName()
            val locked = if (ToolRegistry.isMandatoryTool(name)) "  🔒 core" else ""
            "${tool.getDisplayName()}\n$name$locked"
        }.toTypedArray()

        val checked = tools.map { ToolRegistry.isToolEnabled(it.getName()) }.toBooleanArray()
        val pendingDisabled = ToolRegistry.getDisabledToolNames().toMutableSet()

        android.app.AlertDialog.Builder(context)
            .setTitle("Manage Tools")
            .setMultiChoiceItems(labels, checked) { dialog, which, isChecked ->
                val toolName = tools[which].getName()
                if (ToolRegistry.isMandatoryTool(toolName)) {
                    (dialog as? android.app.AlertDialog)?.listView?.setItemChecked(which, true)
                    Toast.makeText(context, "Core tool cannot be disabled: $toolName", Toast.LENGTH_SHORT).show()
                    return@setMultiChoiceItems
                }

                if (isChecked) {
                    pendingDisabled.remove(toolName)
                } else {
                    pendingDisabled.add(toolName)
                }
            }
            .setPositiveButton("Save") { _, _ ->
                ToolRegistry.setDisabledToolNames(pendingDisabled)
                onChanged()
                Toast.makeText(context, summary(), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Reset") { _, _ ->
                ToolRegistry.resetToolSettings()
                onChanged()
                Toast.makeText(context, "All tools enabled", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun ensureMobileToolsRegistered() {
        if (ToolRegistry.getTotalToolCount() == 0) {
            ToolRegistry.registerAllTools(ToolRegistry.DeviceType.MOBILE)
        }
    }
}
