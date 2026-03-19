/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.event.variables

import android.text.InputFilter
import android.text.InputType
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
    private lateinit var valueBooleanSpinner: Spinner
    private lateinit var valueContainer: LinearLayout

    override fun onCreateView(): ViewGroup {
        val types = arrayOf("Inteiro (I)", "Booleano (B)", "String (S)")
        val booleanValues = arrayOf("Falso", "Verdadeiro")

        nameInput = EditText(context).apply {
            hint = "Nome da variável"
            setText(existing?.name ?: "")
            inputType = InputType.TYPE_CLASS_TEXT
        }

        typeSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, types).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(selectedType)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selectedType = pos
                    updateValueField()
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }

        valueInput = EditText(context).apply {
            hint = "Valor"
        }

        valueBooleanSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, booleanValues).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            // Set initial value for boolean
            val initialBool = (existing as? Variable.BooleanVar)?.value ?: false
            setSelection(if (initialBool) 1 else 0)
        }

        val btnOk = Button(context).apply {
            text = "OK"
            setOnClickListener {
                val name = nameInput.text.toString().trim()
                if (name.isEmpty()) return@setOnClickListener
                val variable = when (selectedType) {
                    0 -> Variable.NumberVar(name, valueInput.text.toString().toIntOrNull() ?: 0)
                    1 -> Variable.BooleanVar(name, valueBooleanSpinner.selectedItemPosition == 1)
                    else -> Variable.TextVar(name, valueInput.text.toString())
                }
                onComplete(variable)
                back()
            }
        }

        val btnCancel = Button(context).apply {
            text = "Cancelar"
            setOnClickListener { back() }
        }

        val buttonsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.END
            addView(btnCancel)
            addView(btnOk)
        }

        valueContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { text = "Valor:"; setPadding(0, 16, 0, 4) })
            addView(valueInput)
            addView(valueBooleanSpinner)
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
            addView(valueContainer)
            addView(buttonsRow)
        }

        // Set initial value
        when (existing) {
            is Variable.NumberVar -> valueInput.setText(existing.value.toString())
            is Variable.TextVar -> valueInput.setText(existing.value)
            else -> {}
        }

        // Apply initial field state
        updateValueField()

        return root as ViewGroup
    }

    private fun updateValueField() {
        when (selectedType) {
            0 -> { // Número inteiro
                valueInput.visibility = View.VISIBLE
                valueInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                valueInput.filters = arrayOf(InputFilter.LengthFilter(10))
                valueBooleanSpinner.visibility = View.GONE
            }
            1 -> { // Booleano
                valueInput.visibility = View.GONE
                valueBooleanSpinner.visibility = View.VISIBLE
            }
            2 -> { // String de texto
                valueInput.visibility = View.VISIBLE
                valueInput.inputType = InputType.TYPE_CLASS_TEXT
                valueInput.filters = arrayOf()
                valueBooleanSpinner.visibility = View.GONE
            }
        }
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {}
}
