/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.feature.smart.config.ui.condition.trigger.timeofday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buzbuz.smartautoclicker.core.domain.model.condition.TriggerCondition
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
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class TimeOfDayConditionViewModel @Inject constructor(
    private val editionRepository: EditionRepository,
) : ViewModel() {

    private val configuredCondition: Flow<TriggerCondition.OnTimeOfDayReached> =
        editionRepository.editionState.editedTriggerConditionState
            .mapNotNull { it.value }
            .filterIsInstance<TriggerCondition.OnTimeOfDayReached>()

    private val editedConditionHasChanged: StateFlow<Boolean> =
        editionRepository.editionState.editedTriggerConditionState
            .map { it.hasChanged }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isEditingCondition: Flow<Boolean> = editionRepository.isEditingCondition
        .distinctUntilChanged()
        .debounce(1000)

    val name: Flow<String?> = configuredCondition.map { it.name }.take(1)
    val nameError: Flow<Boolean> = configuredCondition.map { it.name.isEmpty() }
    val hour: Flow<Int> = configuredCondition.map { it.hour }
    val minute: Flow<Int> = configuredCondition.map { it.minute }
    val daysOfWeek: Flow<Set<DayOfWeek>> = configuredCondition.map { it.daysOfWeek }
    val specificDate: Flow<LocalDate?> = configuredCondition.map { it.specificDate }

    val conditionCanBeSaved: Flow<Boolean> = editionRepository.editionState.editedTriggerConditionState
        .map { it.canBeSaved }

    fun hasUnsavedModifications(): Boolean = editedConditionHasChanged.value

    fun setName(name: String) {
        updateEditedCondition { it.copy(name = name) }
    }

    fun setHour(hour: Int) {
        updateEditedCondition { it.copy(hour = hour) }
    }

    fun setMinute(minute: Int) {
        updateEditedCondition { it.copy(minute = minute) }
    }

    fun toggleDayOfWeek(day: DayOfWeek) {
        updateEditedCondition { condition ->
            val newDays = if (day in condition.daysOfWeek)
                condition.daysOfWeek - day
            else
                condition.daysOfWeek + day
            condition.copy(daysOfWeek = newDays, specificDate = null)
        }
    }

    fun setSpecificDate(date: LocalDate?) {
        updateEditedCondition { it.copy(specificDate = date, daysOfWeek = emptySet()) }
    }

    fun clearSchedule() {
        updateEditedCondition { it.copy(daysOfWeek = emptySet(), specificDate = null) }
    }

    private fun updateEditedCondition(
        closure: (oldValue: TriggerCondition.OnTimeOfDayReached) -> TriggerCondition.OnTimeOfDayReached?,
    ) {
        editionRepository.editionState.getEditedCondition<TriggerCondition.OnTimeOfDayReached>()?.let { condition ->
            closure(condition)?.let { newValue ->
                editionRepository.updateEditedCondition(newValue)
            }
        }
    }
}
