/*
 * Copyright (C) 2025 AIClicker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.buzbuz.smartautoclicker.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 18 to 19.
 * Adds support for ON_TIME_OF_DAY_REACHED trigger condition.
 */
object Migration18to19 : Migration(18, 19) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE condition_table ADD COLUMN time_hour INTEGER")
        db.execSQL("ALTER TABLE condition_table ADD COLUMN time_minute INTEGER")
        db.execSQL("ALTER TABLE condition_table ADD COLUMN time_days_of_week TEXT")
        db.execSQL("ALTER TABLE condition_table ADD COLUMN time_date TEXT")
    }
}
