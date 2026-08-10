package com.aman.vanish.ai

import android.util.LruCache
import com.google.gson.Gson
import org.koitharu.kotatsu.parsers.model.Manga
import com.aman.vanish.ai.models.AiStructuredFilters

object SearchResultsCache {
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long
    )

    private val cache = LruCache<String, CacheEntry<List<Pair<Manga, String>>>>(20)
    private const val TTL_MS = 5 * 60 * 1000L // 5 minutes
    private val gson = Gson()

    fun get(filters: AiStructuredFilters): List<Pair<Manga, String>>? {
        val key = gson.toJson(filters)
        val entry = synchronized(cache) { cache.get(key) } ?: return null
        if (System.currentTimeMillis() - entry.timestamp > TTL_MS) {
            synchronized(cache) { cache.remove(key) }
            return null
        }
        return entry.data
    }

    fun put(filters: AiStructuredFilters, results: List<Pair<Manga, String>>) {
        val key = gson.toJson(filters)
        val entry = CacheEntry(results, System.currentTimeMillis())
        synchronized(cache) { cache.put(key, entry) }
    }

    fun clear() {
        synchronized(cache) { cache.evictAll() }
    }
}
