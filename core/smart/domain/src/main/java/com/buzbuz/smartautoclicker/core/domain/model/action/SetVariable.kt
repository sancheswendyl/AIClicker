/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.core.domain.model.action

import com.buzbuz.smartautoclicker.core.base.identifier.Identifier
import com.buzbuz.smartautoclicker.core.domain.model.VariableType
import com.buzbuz.smartautoclicker.core.domain.model.VariableOperation

data class SetVariable(
    override val id: Identifier,
    override val eventId: Identifier,
    override val name: String? = null,
    override var priority: Int,
    val variableName: String,
    val variableType: VariableType,
    val operation: VariableOperation,
    val valueNumber: Int = 0,
    val valueBoolean: Boolean = false,
    val valueText: String = "",
) : Action() {

    override fun isComplete(): Boolean =
        super.isComplete() && variableName.isNotEmpty()

    override fun hashCodeNoIds(): Int =
        name.hashCode() + variableName.hashCode() + variableType.hashCode() +
        operation.hashCode() + valueNumber.hashCode() + valueBoolean.hashCode() + valueText.hashCode()

    override fun deepCopy(): SetVariable = copy(
        name = "" + name,
        variableName = "" + variableName,
    )
}
