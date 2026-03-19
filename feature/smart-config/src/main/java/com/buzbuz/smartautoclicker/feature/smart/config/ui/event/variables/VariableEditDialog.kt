/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.event.variables

import android.app.AlertDialog
import android.content.Context
import com.buzbuz.smartautoclicker.core.domain.model.Variable
import com.buzbuz.smartautoclicker.core.domain.model.VariableType
import com.buzbuz.smartautoclicker.feature.smart.config.R

object VariableEditDialog {

    fun showCreateDialog(
        context: Context,
        onVariableCreated: (Variable) -> Unit,
    ) {
        showDialog(context, null, onVariableCreated)
    }

    fun showEditDialog(
        context: Context,
        variable: Variable,
        onVariableUpdated: (Variable) -> Unit,
    ) {
        showDialog(context, variable, onVariableUpdated)
    }

    private fun showDialog(
        context: Context,
        existing: Variable?,
        onComplete: (Variable) -> Unit,
    ) {
        val types = arrayOf("Número (I)", "Booleano (B)", "Texto (S)")
        var selectedType = when (existing) {
            is Variable.NumberVar -> 0
            is Variable.BooleanVar -> 1
            is Variable.TextVar -> 2
            null -> 0
        }

        val nameInput = android.widget.EditText(context).apply {
            hint = "Nome da variável"
            setText(existing?.name ?: "")
        }

        val typeSpinner = android.widget.Spinner(context).apply {
            adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_spinner_item, types).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(selectedType)
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    selectedType = position
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }

        val valueInput = android.widget.EditText(context).apply {
            hint = "Valor inicial"
            setText(existing?.getDisplayValue() ?: "")
        }

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(android.widget.TextView(context).apply { text = "Nome:" })
            addView(nameInput)
            addView(android.widget.TextView(context).apply { text = "Tipo:"; setPadding(0, 16, 0, 0) })
            addView(typeSpinner)
            addView(android.widget.TextView(context).apply { text = "Valor:"; setPadding(0, 16, 0, 0) })
            addView(valueInput)
        }

        AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(if (existing == null) "Nova variável" else "Editar variável")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isEmpty()) return@setPositiveButton
                val valueStr = valueInput.text.toString().trim()
                val variable = when (selectedType) {
                    0 -> Variable.NumberVar(name, valueStr.toIntOrNull() ?: 0)
                    1 -> Variable.BooleanVar(name, valueStr.lowercase() == "verdadeiro" || valueStr.lowercase() == "true")
                    else -> Variable.TextVar(name, valueStr)
                }
                onComplete(variable)
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .apply {
                window?.setType(android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            }
            .show()
    }
}
