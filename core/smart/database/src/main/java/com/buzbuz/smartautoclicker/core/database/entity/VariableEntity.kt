/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.buzbuz.smartautoclicker.core.base.interfaces.EntityWithId
import kotlinx.serialization.Serializable

/**
 * Entity defining a variable that can be used in procedures.
 *
 * @param id unique identifier for a variable.
 * @param scenarioId identifier of the scenario this variable belongs to.
 * @param name the name of the variable.
 * @param type the type of the variable (NUMBER, BOOLEAN, TEXT).
 * @param valueNumber the numeric value if type is NUMBER.
 * @param valueBoolean the boolean value if type is BOOLEAN.
 * @param valueText the text value if type is TEXT.
 */
@Entity(
    tableName = "variable_table",
    indices = [Index("scenarioId")],
    foreignKeys = [ForeignKey(
        entity = ScenarioEntity::class,
        parentColumns = ["id"],
        childColumns = ["scenarioId"],
        onDelete = ForeignKey.CASCADE
    )]
)
@Serializable
data class VariableEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override var id: Long,
    @ColumnInfo(name = "scenarioId") var scenarioId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: VariableType,
    @ColumnInfo(name = "value_number", defaultValue = "0") val valueNumber: Int = 0,
    @ColumnInfo(name = "value_boolean", defaultValue = "0") val valueBoolean: Boolean = false,
    @ColumnInfo(name = "value_text", defaultValue = "") val valueText: String = "",
) : EntityWithId
