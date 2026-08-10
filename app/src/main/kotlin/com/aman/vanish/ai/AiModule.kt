package com.aman.vanish.ai

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.aman.vanish.ai.db.AiCacheEntryDao
import com.aman.vanish.ai.db.AiPromptHistoryDao
import com.aman.vanish.core.db.MangaDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiPromptHistoryDao(db: MangaDatabase): AiPromptHistoryDao =
        db.getAiPromptHistoryDao()

    @Provides
    @Singleton
    fun provideAiCacheEntryDao(db: MangaDatabase): AiCacheEntryDao =
        db.getAiCacheEntryDao()
}
