/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.core.domain.model

import com.buzbuz.smartautoclicker.core.database.entity.VariableEntity
import com.buzbuz.smartautoclicker.core.database.entity.VariableType

/** Convert a domain Variable to a database VariableEntity. */
internal fun Variable.toEntity(scenarioId: Long): VariableEntity =
    when (this) {
        is Variable.NumberVar -> VariableEntity(
            id = 0,
            scenarioId = scenarioId,
            name = name,
            type = VariableType.NUMBER,
            valueNumber = value,
        )
        is Variable.BooleanVar -> VariableEntity(
            id = 0,
            scenarioId = scenarioId,
            name = name,
            type = VariableType.BOOLEAN,
            valueBoolean = value,
        )
        is Variable.TextVar -> VariableEntity(
            id = 0,
            scenarioId = scenarioId,
            name = name,
            type = VariableType.TEXT,
            valueText = value,
        )
    }

/** Convert a database VariableEntity to a domain Variable. */
internal fun VariableEntity.toDomain(): Variable =
    when (type) {
        VariableType.NUMBER -> Variable.NumberVar(
            name = name,
            value = valueNumber,
        )
        VariableType.BOOLEAN -> Variable.BooleanVar(
            name = name,
            value = valueBoolean,
        )
        VariableType.TEXT -> Variable.TextVar(
            name = name,
            value = valueText,
        )
    }
