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
 * Migration from database version 20 to 21.
 * Adds support for SetVariable action.
 */
object Migration20to21 : Migration(20, 21) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE action_table ADD COLUMN variable_name TEXT")
        db.execSQL("ALTER TABLE action_table ADD COLUMN variable_type TEXT")
        db.execSQL("ALTER TABLE action_table ADD COLUMN variable_operation TEXT")
        db.execSQL("ALTER TABLE action_table ADD COLUMN variable_value_number INTEGER")
        db.execSQL("ALTER TABLE action_table ADD COLUMN variable_value_boolean INTEGER")
        db.execSQL("ALTER TABLE action_table ADD COLUMN variable_value_text TEXT")
    }
}
