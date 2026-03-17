/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.core.processing.data.processor.state

import com.buzbuz.smartautoclicker.core.domain.model.condition.TriggerCondition
import java.time.LocalDate
import java.time.LocalTime

interface ITimeOfDayState {
    fun isTimeOfDayReached(condition: TriggerCondition.OnTimeOfDayReached): Boolean
}

internal class TimeOfDayState : ITimeOfDayState {

    override fun isTimeOfDayReached(condition: TriggerCondition.OnTimeOfDayReached): Boolean {
        val now = LocalTime.now()
        val conditionTime = LocalTime.of(condition.hour, condition.minute)

        // Check if current time matches (within the same minute)
        if (now.hour != conditionTime.hour || now.minute != conditionTime.minute) return false

        // If specific date is set, check if today matches
        if (condition.specificDate != null) {
            return LocalDate.now() == condition.specificDate
        }

        // If days of week are set, check if today matches
        if (condition.daysOfWeek.isNotEmpty()) {
            return LocalDate.now().dayOfWeek in condition.daysOfWeek
        }

        // No date/day restriction, trigger every day at this time
        return true
    }
}
