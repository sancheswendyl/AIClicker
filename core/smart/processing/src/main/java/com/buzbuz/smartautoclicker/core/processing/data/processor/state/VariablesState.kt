/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.core.processing.data.processor.state

import com.buzbuz.smartautoclicker.core.domain.model.Variable
import com.buzbuz.smartautoclicker.core.domain.model.VariableOperation
import com.buzbuz.smartautoclicker.core.domain.model.VariableComparisonOperation

interface IVariablesState {
    fun getVariable(name: String): Variable?
    fun setVariable(name: String, value: Any, operation: VariableOperation = VariableOperation.SET)
    fun evaluateCondition(name: String, operation: VariableComparisonOperation, value: Any): Boolean
}

internal class VariablesState : IVariablesState {

    private val variableMap: MutableMap<String, Variable> = mutableMapOf()

    override fun getVariable(name: String): Variable? = variableMap[name]

    override fun setVariable(name: String, value: Any, operation: VariableOperation) {
        val current = variableMap[name]

        val newVariable = when {
            // NumberVar operations
            value is Int && (current == null || current is Variable.NumberVar) -> {
                val currentValue = (current as? Variable.NumberVar)?.value ?: 0
                val newValue = when (operation) {
                    VariableOperation.SET -> value
                    VariableOperation.ADD -> currentValue + value
                    VariableOperation.MINUS -> currentValue - value
                    else -> value
                }
                Variable.NumberVar(name, newValue)
            }

            // BooleanVar operations
            value is Boolean && (current == null || current is Variable.BooleanVar) -> {
                val currentValue = (current as? Variable.BooleanVar)?.value ?: false
                val newValue = when (operation) {
                    VariableOperation.TOGGLE -> !currentValue
                    else -> value
                }
                Variable.BooleanVar(name, newValue)
            }

            // TextVar operations
            value is String && (current == null || current is Variable.TextVar) -> {
                val currentValue = (current as? Variable.TextVar)?.value ?: ""
                val newValue = when (operation) {
                    VariableOperation.APPEND -> currentValue + value
                    else -> value
                }
                Variable.TextVar(name, newValue)
            }

            else -> return
        }

        variableMap[name] = newVariable
    }

    override fun evaluateCondition(
        name: String,
        operation: VariableComparisonOperation,
        value: Any,
    ): Boolean {
        val variable = variableMap[name] ?: return false

        return when (variable) {
            is Variable.NumberVar -> {
                val compareValue = value as? Int ?: return false
                when (operation) {
                    VariableComparisonOperation.EQUALS -> variable.value == compareValue
                    VariableComparisonOperation.NOT_EQUALS -> variable.value != compareValue
                    VariableComparisonOperation.GREATER -> variable.value > compareValue
                    VariableComparisonOperation.GREATER_OR_EQUALS -> variable.value >= compareValue
                    VariableComparisonOperation.LOWER -> variable.value < compareValue
                    VariableComparisonOperation.LOWER_OR_EQUALS -> variable.value <= compareValue
                    else -> false
                }
            }

            is Variable.BooleanVar -> {
                val compareValue = value as? Boolean ?: return false
                when (operation) {
                    VariableComparisonOperation.EQUALS -> variable.value == compareValue
                    VariableComparisonOperation.NOT_EQUALS -> variable.value != compareValue
                    else -> false
                }
            }

            is Variable.TextVar -> {
                val compareValue = value as? String ?: return false
                when (operation) {
                    VariableComparisonOperation.EQUALS -> variable.value == compareValue
                    VariableComparisonOperation.NOT_EQUALS -> variable.value != compareValue
                    VariableComparisonOperation.CONTAINS -> variable.value.contains(compareValue)
                    VariableComparisonOperation.NOT_CONTAINS -> !variable.value.contains(compareValue)
                    else -> false
                }
            }
        }
    }

    fun clearAll() {
        variableMap.clear()
    }
}
