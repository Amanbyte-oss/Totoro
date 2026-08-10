package com.aman.vanish.ai

import com.aman.vanish.ai.db.AiCacheEntryDao
import com.aman.vanish.ai.db.AiCacheEntryEntity
import org.koitharu.kotatsu.parsers.model.Manga
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed disk cache for AI search results.
 * TTL: 1 hour per entry. Keys are exact prompt strings (lowercased + trimmed).
 */
@Singleton
class AiDiskCacheRepository @Inject constructor(
    private val dao: AiCacheEntryDao,
) {
    companion object {
        private const val TTL_MS = 60L * 60 * 1000 // 1 hour
    }

    data class CachedResult(
        val manga: List<Pair<Manga, String>>,
        val source: String,
    )

    /** Returns a cached result if it exists and is fresh (< 1 hour old), otherwise null. */
    suspend fun getIfFresh(prompt: String): CachedResult? {
        val key = normalizeKey(prompt)
        val entry = dao.getEntry(key) ?: return null
        val age = System.currentTimeMillis() - entry.timestamp
        if (age > TTL_MS) return null
        return try {
            val parsed = deserializeResults(entry.resultsJson)
            CachedResult(manga = parsed, source = entry.source)
        } catch (_: Exception) {
            null
        }
    }

    /** Saves a result to disk cache. */
    suspend fun save(
        prompt: String,
        mangaList: List<Pair<Manga, String>>,
        source: String,
    ) {
        val key = normalizeKey(prompt)
        val json = serializeResults(mangaList)
        dao.upsert(
            AiCacheEntryEntity(
                prompt = key,
                resultsJson = json,
                timestamp = System.currentTimeMillis(),
                source = source,
            ),
        )
    }

    private fun normalizeKey(prompt: String) = prompt.trim().lowercase()

    /**
     * Serialize just the stable fields we can reconstruct a lightweight display object from.
     * Full Manga objects are rehydrated from their source on detail open.
     */
    private fun serializeResults(list: List<Pair<Manga, String>>): String {
        val arr = JSONArray()
        for ((manga, sourceName) in list) {
            val obj = JSONObject()
            obj.put("id", manga.id)
            obj.put("title", manga.title)
            obj.put("coverUrl", manga.coverUrl ?: "")
            obj.put("source", manga.source.name)
            obj.put("sourceName", sourceName)
            arr.put(obj)
        }
        return arr.toString()
    }

    /**
     * Deserialize lightweight display-only Manga stubs from JSON.
     * NOTE: These are display stubs only; for full detail navigation the source
     * is re-queried via the existing router flow.
     */
    private fun deserializeResults(json: String): List<Pair<Manga, String>> {
        val arr = JSONArray(json)
        val result = mutableListOf<Pair<Manga, String>>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val sourceEnum = runCatching {
                org.koitharu.kotatsu.parsers.model.MangaParserSource.valueOf(obj.getString("source"))
            }.getOrNull() ?: continue
            val manga = Manga(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                altTitles = emptySet(),
                url = "",
                publicUrl = "",
                rating = 0f,
                contentRating = org.koitharu.kotatsu.parsers.model.ContentRating.SAFE,
                coverUrl = obj.optString("coverUrl", ""),
                tags = emptySet(),
                state = null,
                authors = emptySet(),
                largeCoverUrl = null,
                description = null,
                chapters = null,
                source = sourceEnum,
            )
            result.add(Pair(manga, obj.getString("sourceName")))
        }
        return result
    }
}
