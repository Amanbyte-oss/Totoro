package com.aman.vanish.ai.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_cache_entries")
data class AiCacheEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "prompt") val prompt: String,
    @ColumnInfo(name = "results_json") val resultsJson: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    // "ai" or "fallback"
    @ColumnInfo(name = "source") val source: String = "ai",
)
