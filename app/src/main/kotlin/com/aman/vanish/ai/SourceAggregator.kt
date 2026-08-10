package com.aman.vanish.ai

import android.util.Log
import com.aman.vanish.ai.models.AiStructuredFilters
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaParserSource
import org.koitharu.kotatsu.parsers.model.MangaListFilter
import com.aman.vanish.core.parser.MangaRepository
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceAggregator @Inject constructor(
    private val repositoryFactory: MangaRepository.Factory
) {
    // Parallel map mapping Manga to source name (for tagging metadata)
    val mangaToSourceMap = ConcurrentHashMap<Manga, String>()

    companion object {
        private const val TAG = "AiPickSource"
    }

    suspend fun searchAll(filters: AiStructuredFilters): Result<SearchResult> = supervisorScope {
        mangaToSourceMap.clear()
        val failedSources = Collections.synchronizedSet(mutableSetOf<String>())

        val deferredResults = ALLOWED_SOURCES.map { sourceName ->
            // 3.C — Parallel execution safety via supervised async job
            async {
                val parserSource = getParserSourceByName(sourceName)
                if (parserSource == null) {
                    Log.w(TAG, "Source '$sourceName' not found in parser sources")
                    failedSources.add("$sourceName (missing source)")
                    return@async emptyList<Manga>()
                }

                try {
                    val repository = repositoryFactory.create(parserSource)
                    Log.d(TAG, "Querying source: $sourceName, parser: ${repository.javaClass.simpleName}")

                    var results: List<Manga> = emptyList()
                    var success = false

                    // Attempt 1: Full translated filter (15-second timeout)
                    try {
                        val translatedFilter = FilterTranslator.translate(filters, repository)
                        results = withTimeout(15000) {
                            repository.getList(0, null, translatedFilter)
                        }
                        success = true
                        Log.d(TAG, "Source $sourceName Attempt 1 success: ${results.size} items")
                    } catch (e: Exception) {
                        Log.w(TAG, "Source $sourceName Attempt 1 failed: ${e.message}. Trying Attempt 2...")
                    }

                    // Attempt 2: Keyword query search if Attempt 1 failed (15-second timeout)
                    if (!success) {
                        val queryText = filters.genres.joinToString(" ")
                        if (queryText.isNotEmpty()) {
                            try {
                                results = withTimeout(15000) {
                                    repository.getList(0, null, MangaListFilter(query = queryText))
                                }
                                results = filterClientSide(results, filters)
                                success = true
                                Log.d(TAG, "Source $sourceName Attempt 2 success: ${results.size} items")
                            } catch (e: Exception) {
                                Log.w(TAG, "Source $sourceName Attempt 2 failed: ${e.message}. Trying Attempt 3...")
                            }
                        }
                    }

                    // Attempt 3: General listing getList(0, null, null) if prior failed (15-second timeout)
                    if (!success) {
                        try {
                            results = withTimeout(15000) {
                                repository.getList(0, null, null)
                            }
                            results = filterClientSide(results, filters)
                            success = true
                            Log.d(TAG, "Source $sourceName Attempt 3 success: ${results.size} items")
                        } catch (e: Exception) {
                            Log.w(TAG, "Source $sourceName Attempt 3 failed: ${e.message}. Trying Attempt 4...")
                        }
                    }

                    // Attempt 4: General listing getList(0, null, MangaListFilter.EMPTY) if prior failed (15-second timeout)
                    if (!success) {
                        try {
                            results = withTimeout(15000) {
                                repository.getList(0, null, MangaListFilter.EMPTY)
                            }
                            results = filterClientSide(results, filters)
                            success = true
                            Log.d(TAG, "Source $sourceName Attempt 4 success: ${results.size} items")
                        } catch (e: Exception) {
                            Log.e(TAG, "Source $sourceName all attempts failed: ${e.message}", e)
                            throw e
                        }
                    }

                    results.forEach { manga ->
                        mangaToSourceMap[manga] = sourceName
                    }
                    results
                } catch (e: Exception) {
                    val reason = getExceptionReason(e)
                    Log.e(TAG, "Source $sourceName query failed: $reason", e)
                    failedSources.add("$sourceName ($reason)")
                    emptyList<Manga>()
                }
            }
        }

        // Overall aggregator timeout: 30 seconds
        val allResults = try {
            withTimeout(30000) {
                deferredResults.awaitAll()
                    .sortedByDescending { it.isNotEmpty() }
                    .flatten()
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Overall aggregator timed out after 30 seconds, gathering completed partial results")
            deferredResults.mapNotNull {
                if (it.isCompleted) {
                    try {
                        it.getCompleted()
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
            }
            .sortedByDescending { it.isNotEmpty() }
            .flatten()
        }

        val failed = failedSources.toList()

        // 2. — Specific error messages and check if ALL active sources failed
        if (failed.size == ALLOWED_SOURCES.size) {
            Result.failure(java.io.IOException("All manga sources are offline. Check your internet."))
        } else {
            Result.success(SearchResult(allResults, failed))
        }
    }

    private fun getExceptionReason(e: Exception): String {
        return when (e) {
            is java.net.UnknownHostException -> "offline"
            is javax.net.ssl.SSLException -> "SSL error"
            is java.net.SocketTimeoutException -> "timeout"
            else -> e.message ?: "error"
        }
    }

    private fun filterClientSide(list: List<Manga>, filters: AiStructuredFilters): List<Manga> {
        return list.filter { manga ->
            val matchesGenre = filters.genres.isEmpty() || filters.genres.any { genre ->
                manga.tags.any { tag -> tag.title.equals(genre, ignoreCase = true) }
            }
            val matchesType = filters.type?.let { type ->
                manga.title.contains(type, ignoreCase = true) ||
                manga.tags.any { tag -> tag.title.equals(type, ignoreCase = true) }
            } ?: true
            val matchesStatus = filters.status?.let { status ->
                manga.state?.name?.equals(status, ignoreCase = true) == true
            } ?: true
            val matchesDemographic = filters.demographic?.let { demographic ->
                manga.tags.any { tag -> tag.title.equals(demographic, ignoreCase = true) }
            } ?: true

            matchesGenre && matchesType && matchesStatus && matchesDemographic
        }
    }

    private fun getParserSourceByName(name: String): MangaParserSource? {
        return MangaParserSource.values().find {
            it.title.equals(name, ignoreCase = true) || it.name.equals(name.replace(" ", "_"), ignoreCase = true)
        }
    }
}

data class SearchResult(
    val mangaList: List<Manga>,
    val failedSources: List<String>
)

