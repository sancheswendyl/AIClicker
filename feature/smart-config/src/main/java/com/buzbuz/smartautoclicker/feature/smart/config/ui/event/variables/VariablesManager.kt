/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.event.variables

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buzbuz.smartautoclicker.core.common.overlays.manager.OverlayManager
import com.buzbuz.smartautoclicker.core.domain.model.Variable
import com.buzbuz.smartautoclicker.feature.smart.config.R

class VariablesManager(
    private val context: Context,
    private val overlayManager: OverlayManager,
    private val btnTab: View,
    private val iconTab: android.widget.ImageView,
    private val recyclerView: RecyclerView,
    private val onVariablesChanged: (List<Variable>) -> Unit,
) {
    private var isExpanded = false
    private val variables = mutableListOf<Variable>()

    private val adapter = VariablesAdapter(
        onVariableClicked = { variable -> editVariable(variable) },
        onVariableDeleted = { variable -> deleteVariable(variable) },
    )

    init {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@VariablesManager.adapter
        }
        btnTab.setOnClickListener { toggleExpand() }
    }

    fun updateVariables(newVariables: List<Variable>) {
        variables.clear()
        variables.addAll(newVariables)
        adapter.submitList(variables.toList())
    }

    fun addVariable() {
        overlayManager.navigateTo(
            context = context,
            newOverlay = VariableEditOverlayDialog(
                existing = null,
                onComplete = { newVariable ->
                    variables.removeAll { it.name == newVariable.name }
                    variables.add(newVariable)
                    adapter.submitList(variables.toList())
                    onVariablesChanged(variables.toList())
                },
            ),
            hideCurrent = false,
        )
    }

    private fun editVariable(variable: Variable) {
        overlayManager.navigateTo(
            context = context,
            newOverlay = VariableEditOverlayDialog(
                existing = variable,
                onComplete = { updated ->
                    val index = variables.indexOfFirst { it.name == variable.name }
                    if (index >= 0) {
                        variables[index] = updated
                        adapter.submitList(variables.toList())
                        onVariablesChanged(variables.toList())
                    }
                },
            ),
            hideCurrent = false,
        )
    }

    private fun deleteVariable(variable: Variable) {
        variables.removeAll { it.name == variable.name }
        adapter.submitList(variables.toList())
        onVariablesChanged(variables.toList())
    }

    private fun toggleExpand() {
        isExpanded = !isExpanded
        recyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
        iconTab.setImageResource(
            if (isExpanded) R.drawable.ic_chevron_down
            else R.drawable.ic_chevron_up
        )
    }
}
