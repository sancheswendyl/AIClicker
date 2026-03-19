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
 * Migration from database version 19 to 20.
 * Adds support for variables in procedures.
 */
object Migration19to20 : Migration(19, 20) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS variable_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                scenarioId INTEGER NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                value_number INTEGER NOT NULL DEFAULT 0,
                value_boolean INTEGER NOT NULL DEFAULT 0,
                value_text TEXT NOT NULL DEFAULT '',
                FOREIGN KEY (scenarioId) REFERENCES scenario_table(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS index_variable_table_scenarioId ON variable_table(scenarioId)")
    }
}
