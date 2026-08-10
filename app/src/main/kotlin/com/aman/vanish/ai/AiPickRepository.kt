package com.aman.vanish.ai

import com.aman.vanish.ai.models.AiStructuredFilters
import org.koitharu.kotatsu.parsers.model.Manga

interface AiPickRepository {
    suspend fun parseUserQuery(text: String): Result<AiStructuredFilters>
    suspend fun searchSources(filters: AiStructuredFilters): Result<List<Manga>>
}
