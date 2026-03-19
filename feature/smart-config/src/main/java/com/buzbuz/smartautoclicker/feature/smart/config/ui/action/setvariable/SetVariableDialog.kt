/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.action.setvariable

import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.buzbuz.smartautoclicker.core.common.overlays.base.viewModels
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.OverlayDialog
import com.buzbuz.smartautoclicker.core.domain.model.VariableType
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.DialogNavigationButton
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.setButtonEnabledState
import com.buzbuz.smartautoclicker.core.ui.bindings.dropdown.setItems
import com.buzbuz.smartautoclicker.core.ui.bindings.dropdown.setSelectedItem
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setError
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setLabel
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setOnTextChangedListener
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setText
import com.buzbuz.smartautoclicker.core.ui.utils.MinMaxInputFilter
import com.buzbuz.smartautoclicker.feature.smart.config.R
import com.buzbuz.smartautoclicker.feature.smart.config.databinding.DialogConfigActionSetVariableBinding
import com.buzbuz.smartautoclicker.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.buzbuz.smartautoclicker.feature.smart.config.ui.action.OnActionConfigCompleteListener
import com.buzbuz.smartautoclicker.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class SetVariableDialog(
    private val listener: OnActionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    private val viewModel: SetVariableViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { setVariableViewModel() },
    )

    private lateinit var viewBinding: DialogConfigActionSetVariableBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigActionSetVariableBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.dialog_title_set_variable)
                buttonDismiss.setDebouncedOnClickListener { back() }
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener {
                        listener.onConfirmClicked()
                        super.back()
                    }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener {
                        listener.onDeleteClicked()
                        super.back()
                    }
                }
            }

            fieldName.apply {
                setLabel(R.string.generic_name)
                setOnTextChangedListener { viewModel.setName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }
            hideSoftInputOnFocusLoss(fieldName.textField)

            fieldVariableName.apply {
                setLabel(R.string.field_variable_name_label)
                setOnTextChangedListener { viewModel.setVariableName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }
            hideSoftInputOnFocusLoss(fieldVariableName.textField)

            fieldVariableType.setItems(
                label = context.getString(R.string.field_variable_type_label),
                items = viewModel.variableTypeItems,
                onItemSelected = viewModel::setTypeItem,
            )

            fieldOperation.setItems(
                label = context.getString(R.string.field_variable_operation_label),
                items = listOf(viewModel.opSetItem),
                onItemSelected = viewModel::setOperationItem,
            )

            fieldValueNumber.apply {
                setLabel(R.string.field_variable_value_label)
                textField.filters = arrayOf(MinMaxInputFilter(Int.MIN_VALUE, Int.MAX_VALUE))
                setOnTextChangedListener {
                    viewModel.setValueNumber(it.toString().toIntOrNull() ?: 0)
                }
            }
            hideSoftInputOnFocusLoss(fieldValueNumber.textField)

            fieldValueBoolean.setItems(
                label = context.getString(R.string.field_variable_value_label),
                items = viewModel.booleanValueItems,
                onItemSelected = viewModel::setBooleanItem,
            )

            fieldValueText.apply {
                setLabel(R.string.field_variable_value_label)
                setOnTextChangedListener { viewModel.setValueText(it.toString()) }
            }
            hideSoftInputOnFocusLoss(fieldValueText.textField)
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingAction.collect(::onActionEditingStateChanged) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.name.collect(viewBinding.fieldName::setText) }
                launch { viewModel.nameError.collect(viewBinding.fieldName::setError) }
                launch { viewModel.variableName.collect(viewBinding.fieldVariableName::setText) }
                launch { viewModel.variableNameError.collect(viewBinding.fieldVariableName::setError) }
                launch { viewModel.selectedTypeItem.collect(viewBinding.fieldVariableType::setSelectedItem) }
                launch { viewModel.operationItems.collect { items ->
                    viewBinding.fieldOperation.setItems(
                        label = context.getString(R.string.field_variable_operation_label),
                        items = items,
                        onItemSelected = viewModel::setOperationItem,
                    )
                }}
                launch { viewModel.selectedOperationItem.collect(viewBinding.fieldOperation::setSelectedItem) }
                launch { viewModel.variableType.collect(::updateValueFieldsVisibility) }
                launch { viewModel.valueNumber.collect { viewBinding.fieldValueNumber.setText(it, InputType.TYPE_CLASS_NUMBER) } }
                launch { viewModel.valueText.collect(viewBinding.fieldValueText::setText) }
                launch { viewModel.selectedBooleanItem.collect(viewBinding.fieldValueBoolean::setSelectedItem) }
                launch { viewModel.isValidAction.collect(::updateSaveButton) }
            }
        }
    }

    override fun back() {
        if (viewModel.hasUnsavedModifications()) {
            context.showCloseWithoutSavingDialog {
                listener.onDismissClicked()
                super.back()
            }
            return
        }
        listener.onDismissClicked()
        super.back()
    }

    private fun updateValueFieldsVisibility(type: VariableType) {
        viewBinding.apply {
            fieldValueNumber.root.visibility = if (type == VariableType.NUMBER) View.VISIBLE else View.GONE
            fieldValueBoolean.root.visibility = if (type == VariableType.BOOLEAN) View.VISIBLE else View.GONE
            fieldValueText.root.visibility = if (type == VariableType.TEXT) View.VISIBLE else View.GONE
        }
    }

    private fun updateSaveButton(isValid: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, isValid)
    }

    private fun onActionEditingStateChanged(isEditing: Boolean) {
        if (!isEditing) finish()
    }
}
