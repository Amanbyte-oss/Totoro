package com.aman.vanish.ai

import android.util.LruCache
import com.aman.vanish.ai.models.AiStructuredFilters

object AiResponseCache {
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long
    )

    private val cache = LruCache<String, CacheEntry<AiStructuredFilters>>(50)
    private const val TTL_MS = 10 * 60 * 1000L // 10 minutes

    fun get(query: String): AiStructuredFilters? {
        val normalized = query.lowercase().trim()
        val entry = synchronized(cache) { cache.get(normalized) } ?: return null
        if (System.currentTimeMillis() - entry.timestamp > TTL_MS) {
            synchronized(cache) { cache.remove(normalized) }
            return null
        }
        return entry.data
    }

    fun put(query: String, filters: AiStructuredFilters) {
        val normalized = query.lowercase().trim()
        val entry = CacheEntry(filters, System.currentTimeMillis())
        synchronized(cache) { cache.put(normalized, entry) }
    }

    fun clear() {
        synchronized(cache) { cache.evictAll() }
    }
}
