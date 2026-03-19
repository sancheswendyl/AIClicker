/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.event.variables

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.OverlayDialog
import com.buzbuz.smartautoclicker.core.domain.model.Variable
import com.buzbuz.smartautoclicker.feature.smart.config.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class VariableEditOverlayDialog(
    private val existing: Variable? = null,
    private val onComplete: (Variable) -> Unit,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    private var selectedType = when (existing) {
        is Variable.NumberVar -> 0
        is Variable.BooleanVar -> 1
        is Variable.TextVar -> 2
        null -> 0
    }

    private lateinit var nameInput: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var valueInput: EditText

    override fun onCreateView(): ViewGroup {
        val types = arrayOf("Número (I)", "Booleano (B)", "Texto (S)")

        nameInput = EditText(context).apply {
            hint = "Nome da variável"
            setText(existing?.name ?: "")
        }

        typeSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, types).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(selectedType)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selectedType = pos
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }

        valueInput = EditText(context).apply {
            hint = "Valor inicial"
            setText(existing?.getDisplayValue() ?: "")
        }

        val btnOk = android.widget.Button(context).apply {
            text = "OK"
            setOnClickListener {
                val name = nameInput.text.toString().trim()
                if (name.isEmpty()) return@setOnClickListener
                val valueStr = valueInput.text.toString().trim()
                val variable = when (selectedType) {
                    0 -> Variable.NumberVar(name, valueStr.toIntOrNull() ?: 0)
                    1 -> Variable.BooleanVar(name, valueStr.lowercase() == "verdadeiro" || valueStr.lowercase() == "true")
                    else -> Variable.TextVar(name, valueStr)
                }
                onComplete(variable)
                back()
            }
        }

        val btnCancel = android.widget.Button(context).apply {
            text = "Cancelar"
            setOnClickListener { back() }
        }

        val buttonsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.END
            addView(btnCancel)
            addView(btnOk)
        }

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(context.getColor(android.R.color.background_dark))

            addView(TextView(context).apply {
                text = if (existing == null) "Nova variável" else "Editar variável"
                textSize = 18f
                setPadding(0, 0, 0, 24)
            })
            addView(TextView(context).apply { text = "Nome:" })
            addView(nameInput)
            addView(TextView(context).apply { text = "Tipo:"; setPadding(0, 16, 0, 4) })
            addView(typeSpinner)
            addView(TextView(context).apply { text = "Valor:"; setPadding(0, 16, 0, 4) })
            addView(valueInput)
            addView(buttonsRow)
        }

        return root as ViewGroup
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {}
}
