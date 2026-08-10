package com.aman.vanish.ai

import com.aman.vanish.ai.db.AiPromptHistoryDao
import com.aman.vanish.ai.db.AiPromptHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiPromptHistoryRepository @Inject constructor(
    private val dao: AiPromptHistoryDao,
) {
    fun getRecentPrompts(limit: Int = 15): Flow<List<AiPromptHistoryEntity>> =
        dao.getRecent(limit)

    suspend fun insertPrompt(prompt: String, wasSuccessful: Boolean) {
        dao.insert(AiPromptHistoryEntity(prompt = prompt, wasSuccessful = wasSuccessful))
    }

    suspend fun clearHistory() = dao.deleteAll()
}
