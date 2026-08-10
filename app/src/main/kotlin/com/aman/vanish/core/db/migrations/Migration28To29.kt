package com.aman.vanish.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration28To29 : Migration(28, 29) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // AI prompt history table
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS ai_prompt_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                prompt TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                was_successful INTEGER NOT NULL
            )""",
        )

        // AI result cache table
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS ai_cache_entries (
                prompt TEXT NOT NULL PRIMARY KEY,
                results_json TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                source TEXT NOT NULL
            )""",
        )
    }
}
