package com.aman.vanish.ai.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiPromptHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AiPromptHistoryEntity)

    @Query("SELECT * FROM ai_prompt_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 15): Flow<List<AiPromptHistoryEntity>>

    @Query("DELETE FROM ai_prompt_history")
    suspend fun deleteAll()
}
