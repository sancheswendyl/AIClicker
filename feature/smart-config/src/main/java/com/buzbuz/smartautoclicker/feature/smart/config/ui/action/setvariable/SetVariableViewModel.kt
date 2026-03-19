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
    val variableName: Flow<String> = configuredAction.map { it.variableName }
    val variableNameError: Flow<Boolean> = configuredAction.map { it.variableName.isEmpty() }
    val variableType: Flow<VariableType> = configuredAction.map { it.variableType }
    val operation: Flow<VariableOperation> = configuredAction.map { it.operation }
    val valueNumber: Flow<Int> = configuredAction.map { it.valueNumber }
    val valueBoolean: Flow<Boolean> = configuredAction.map { it.valueBoolean }
    val valueText: Flow<String> = configuredAction.map { it.valueText }
    val isValidAction: Flow<Boolean> = editionRepository.editionState.editedActionState
        .map { it.canBeSaved }

    // Dropdown items para tipo de variável
    val variableTypeItems = listOf(
        DropdownItem(title = R.string.variable_type_number),
        DropdownItem(title = R.string.variable_type_boolean),
        DropdownItem(title = R.string.variable_type_text),
    )

    // Dropdown items para operação numérica
    val numberOperationItems = listOf(
        DropdownItem(title = R.string.variable_operation_set),
        DropdownItem(title = R.string.variable_operation_add),
        DropdownItem(title = R.string.variable_operation_minus),
    )

    // Dropdown items para operação booleana
    val booleanOperationItems = listOf(
        DropdownItem(title = R.string.variable_operation_set),
        DropdownItem(title = R.string.variable_operation_toggle),
    )

    // Dropdown items para operação texto
    val textOperationItems = listOf(
        DropdownItem(title = R.string.variable_operation_set),
        DropdownItem(title = R.string.variable_operation_append),
    )

    // Dropdown items para valor booleano
    val booleanValueItems = listOf(
        DropdownItem(title = R.string.variable_value_true),
        DropdownItem(title = R.string.variable_value_false),
    )

    fun hasUnsavedModifications(): Boolean = editedActionHasChanged.value

    fun setName(name: String) = updateAction { it.copy(name = name) }
    fun setVariableName(name: String) = updateAction { it.copy(variableName = name) }
    fun setVariableType(type: VariableType) = updateAction { it.copy(variableType = type, operation = VariableOperation.SET) }
    fun setOperation(operation: VariableOperation) = updateAction { it.copy(operation = operation) }
    fun setValueNumber(value: Int) = updateAction { it.copy(valueNumber = value) }
    fun setValueBoolean(value: Boolean) = updateAction { it.copy(valueBoolean = value) }
    fun setValueText(value: String) = updateAction { it.copy(valueText = value) }

    private fun updateAction(closure: (SetVariable) -> SetVariable) {
        editionRepository.editionState.getEditedAction<SetVariable>()?.let { action ->
            editionRepository.updateEditedAction(closure(action))
        }
    }
}
