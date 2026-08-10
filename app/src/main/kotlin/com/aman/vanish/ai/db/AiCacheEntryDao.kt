package com.aman.vanish.ai.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AiCacheEntryDao {

    @Query("SELECT * FROM ai_cache_entries WHERE prompt = :prompt LIMIT 1")
    suspend fun getEntry(prompt: String): AiCacheEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: AiCacheEntryEntity)

    @Query("DELETE FROM ai_cache_entries WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM ai_cache_entries")
    suspend fun deleteAll()
}
