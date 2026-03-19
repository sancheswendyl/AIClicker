/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.buzbuz.smartautoclicker.core.database.entity.VariableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VariableDao {

    @Query("SELECT * FROM variable_table WHERE scenarioId = :scenarioId ORDER BY name ASC")
    fun getVariablesForScenario(scenarioId: Long): Flow<List<VariableEntity>>

    @Query("SELECT * FROM variable_table WHERE scenarioId = :scenarioId ORDER BY name ASC")
    suspend fun getVariablesForScenarioSync(scenarioId: Long): List<VariableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariable(variable: VariableEntity): Long

    @Update
    suspend fun updateVariable(variable: VariableEntity)

    @Delete
    suspend fun deleteVariable(variable: VariableEntity)

    @Query("DELETE FROM variable_table WHERE scenarioId = :scenarioId")
    suspend fun deleteVariablesForScenario(scenarioId: Long)
}
