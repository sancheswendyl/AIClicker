/*
 * Copyright (C) 2025 AIClicker
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.action.setvariable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buzbuz.smartautoclicker.core.domain.model.VariableOperation
import com.buzbuz.smartautoclicker.core.domain.model.VariableType
import com.buzbuz.smartautoclicker.core.domain.model.action.SetVariable
import com.buzbuz.smartautoclicker.core.ui.bindings.dropdown.DropdownItem
import com.buzbuz.smartautoclicker.feature.smart.config.R
import com.buzbuz.smartautoclicker.feature.smart.config.domain.EditionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SetVariableViewModel @Inject constructor(
    private val editionRepository: EditionRepository,
) : ViewModel() {

    // DropdownItems para tipo
    val typeNumberItem = DropdownItem(R.string.variable_type_number)
    val typeBooleanItem = DropdownItem(R.string.variable_type_boolean)
    val typeTextItem = DropdownItem(R.string.variable_type_text)

    // DropdownItems para operação
    val opSetItem = DropdownItem(R.string.variable_operation_set)
    val opAddItem = DropdownItem(R.string.variable_operation_add)
    val opMinusItem = DropdownItem(R.string.variable_operation_minus)
    val opToggleItem = DropdownItem(R.string.variable_operation_toggle)
    val opAppendItem = DropdownItem(R.string.variable_operation_append)

    // DropdownItems para valor booleano
    val boolTrueItem = DropdownItem(R.string.variable_value_true)
    val boolFalseItem = DropdownItem(R.string.variable_value_false)

    val variableTypeItems = listOf(typeNumberItem, typeBooleanItem, typeTextItem)
    val booleanValueItems = listOf(boolTrueItem, boolFalseItem)

    private val configuredAction: Flow<SetVariable> =
        editionRepository.editionState.editedActionState
            .mapNotNull { it.value }
            .filterIsInstance<SetVariable>()

    private val editedActionHasChanged: StateFlow<Boolean> =
        editionRepository.editionState.editedActionState
            .map { it.hasChanged }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isEditingAction: Flow<Boolean> = editionRepository.isEditingAction
        .distinctUntilChanged()
        .debounce(1000)

    val name: Flow<String?> = configuredAction.map { it.name }.take(1)
    val nameError: Flow<Boolean> = configuredAction.map { it.name.isNullOrEmpty() }
    val variableName: Flow<String?> = configuredAction.map { it.variableName }.take(1)
    val variableNameError: Flow<Boolean> = configuredAction.map { it.variableName.isEmpty() }
    val valueNumber: Flow<String?> = configuredAction.map { it.valueNumber.toString() }.take(1)
    val valueText: Flow<String?> = configuredAction.map { it.valueText }.take(1)
    val isValidAction: Flow<Boolean> = editionRepository.editionState.editedActionState.map { it.canBeSaved }

    val selectedTypeItem: Flow<DropdownItem> = configuredAction.map { action ->
        when (action.variableType) {
            VariableType.NUMBER -> typeNumberItem
            VariableType.BOOLEAN -> typeBooleanItem
            VariableType.TEXT -> typeTextItem
        }
    }

    val operationItems: Flow<List<DropdownItem>> = configuredAction.map { action ->
        when (action.variableType) {
            VariableType.NUMBER -> listOf(opSetItem, opAddItem, opMinusItem)
            VariableType.BOOLEAN -> listOf(opSetItem, opToggleItem)
            VariableType.TEXT -> listOf(opSetItem, opAppendItem)
        }
    }

    val selectedOperationItem: Flow<DropdownItem> = configuredAction.map { action ->
        when (action.operation) {
            VariableOperation.SET -> opSetItem
            VariableOperation.ADD -> opAddItem
            VariableOperation.MINUS -> opMinusItem
            VariableOperation.TOGGLE -> opToggleItem
            VariableOperation.APPEND -> opAppendItem
        }
    }

    val selectedBooleanItem: Flow<DropdownItem> = configuredAction.map { action ->
        if (action.valueBoolean) boolTrueItem else boolFalseItem
    }

    val variableType: Flow<VariableType> = configuredAction.map { it.variableType }

    fun hasUnsavedModifications(): Boolean = editedActionHasChanged.value

    fun setName(name: String) = updateAction { it.copy(name = name) }
    fun setVariableName(name: String) = updateAction { it.copy(variableName = name) }

    fun setTypeItem(item: DropdownItem) {
        val type = when (item) {
            typeNumberItem -> VariableType.NUMBER
            typeBooleanItem -> VariableType.BOOLEAN
            else -> VariableType.TEXT
        }
        updateAction { it.copy(variableType = type, operation = VariableOperation.SET) }
    }

    fun setOperationItem(item: DropdownItem) {
        val op = when (item) {
            opAddItem -> VariableOperation.ADD
            opMinusItem -> VariableOperation.MINUS
            opToggleItem -> VariableOperation.TOGGLE
            opAppendItem -> VariableOperation.APPEND
            else -> VariableOperation.SET
        }
        updateAction { it.copy(operation = op) }
    }

    fun setBooleanItem(item: DropdownItem) {
        updateAction { it.copy(valueBoolean = item == boolTrueItem) }
    }

    fun setValueNumber(value: Int) = updateAction { it.copy(valueNumber = value) }
    fun setValueText(value: String) = updateAction { it.copy(valueText = value) }

    private fun updateAction(closure: (SetVariable) -> SetVariable) {
        editionRepository.editionState.getEditedAction<SetVariable>()?.let { action ->
            editionRepository.updateEditedAction(closure(action))
        }
    }
}
