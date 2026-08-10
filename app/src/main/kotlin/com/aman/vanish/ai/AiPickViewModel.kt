package com.aman.vanish.ai

import com.aman.vanish.ai.parser.GroqQueryParser
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koitharu.kotatsu.parsers.model.Manga
import javax.inject.Inject
import com.aman.vanish.ai.db.AiPromptHistoryEntity
import com.aman.vanish.ai.models.AiStructuredFilters
import com.aman.vanish.ai.models.AggregatedManga
import com.aman.vanish.core.os.NetworkState

sealed interface AiPickUiState {
    data object Idle : AiPickUiState
    data class Loading(val message: String) : AiPickUiState
    data class Success(
        val mangaList: List<AggregatedManga>,
        val aiMessage: String
    ) : AiPickUiState
    data class Error(val message: String) : AiPickUiState
}

@HiltViewModel
class AiPickViewModel @Inject constructor(
    private val sourceAggregator: SourceAggregator,
    private val rateLimiter: AIRateLimiter,
    private val historyRepository: AiPromptHistoryRepository,
    private val diskCacheRepository: AiDiskCacheRepository,
    private val analytics: AiPickAnalytics,
    private val networkState: NetworkState,
) : ViewModel() {

    private val queryParser = GroqQueryParser()

    private val _uiState = MutableStateFlow<AiPickUiState>(AiPickUiState.Idle)
    val uiState: StateFlow<AiPickUiState> = _uiState.asStateFlow()

    private var lastQuery: String? = null
    private var lastResult: List<Pair<Manga, String>>? = null
    private var statusCyclingJob: Job? = null
    private var queryJob: Job? = null
    private var retryCount = 0
    private var permanentFallback = false

    /** Exposed StateFlow of today's remaining Groq API quota. */
    val remainingQuota: StateFlow<Int> = rateLimiter.remainingQuota

    /** Exposed StateFlow of recent prompt history (top 15). */
    val recentPrompts: StateFlow<List<AiPromptHistoryEntity>> =
        historyRepository.getRecentPrompts(15)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun sendQuery(userText: String, forceRefresh: Boolean = false) {
        Log.d("GROQ_DEBUG", "ViewModel.sendQuery() called with: $userText")
        // 9.A — Graceful API key guard at runtime
        if (AiConfig.GROQ_API_KEY == "gsk-your-groq-api-key-here") {
            _uiState.value = AiPickUiState.Error("Groq API key not configured")
            return
        }

        val query = userText.trim()
        if (query.isEmpty()) return

        lastQuery = query
        stopStatusCycling()
        _uiState.value = AiPickUiState.Loading("Thinking...")

        // 10. & 1.B — Cancel in-flight queries and launch with SupervisorJob context
        queryJob?.cancel()
        queryJob = viewModelScope.launch(SupervisorJob()) {
            // 8. — Structured Flow logging
            Log.d("AiPickFlow", "Query received: ${query.length} characters")
            
            var wasSuccessful = false
            var filters: AiStructuredFilters? = null
            try {
                // 6. — In-memory caching with silent background refresh
                if (!forceRefresh) {
                    val cachedFilters = AiResponseCache.get(query)
                    if (cachedFilters != null) {
                        val cachedResults = SearchResultsCache.get(cachedFilters)
                        if (cachedResults != null) {
                            lastResult = cachedResults
                            Log.d("AiPickFlow", "In-memory cache hit. Immediately displaying results.")
                            analytics.logCacheHit(query.length)
                            _uiState.value = AiPickUiState.Success(
                                mangaList = cachedResults.map { AggregatedManga(it.first, it.second) },
                                aiMessage = generateAiMessage(cachedFilters)
                            )
                            wasSuccessful = true
                            historyRepository.insertPrompt(query, wasSuccessful = true)
                            
                            // Trigger background refresh silently if online
                            if (networkState.isOnline()) {
                                launch(SupervisorJob()) {
                                    try {
                                        Log.d("AiPickFlow", "Triggering silent background refresh...")
                                        refreshQueryResultsInBackground(query, cachedFilters)
                                    } catch (e: Exception) {
                                        Log.w("AiPickFlow", "Silent background refresh failed: ${e.message}")
                                    }
                                }
                            }
                            return@launch
                        }
                    }
                }

                if (!networkState.isOnline()) {
                    _uiState.value = AiPickUiState.Error("No internet connection. Check your network.")
                    return@launch
                }

                // 11.1 — Rate limit guard
                if (!rateLimiter.canMakeCall()) {
                    analytics.logRateLimitHit()
                    analytics.logError("rate_limit")
                    _uiState.value = AiPickUiState.Error(
                        "Daily AI limit reached. Try again tomorrow or use local search.",
                    )
                    historyRepository.insertPrompt(query, wasSuccessful = false)
                    return@launch
                }

                // 1. Parse query with Groq or fall back to local keyword parsing
                var usedLocalFallback = permanentFallback

                if (permanentFallback) {
                    filters = localKeywordFallback(query)
                } else {
                    val cachedFilters = if (forceRefresh) null else AiResponseCache.get(query)
                    if (cachedFilters != null) {
                        filters = cachedFilters
                    } else {
                        analytics.logPromptSent(query.length)
                        val parseResult = retryWithBackoff(retries = 3) {
                            queryParser.parseUserQuery(query)
                        }
                        
                        var tempFilters: AiStructuredFilters? = null
                        var parseError: Throwable? = null
                        parseResult.onSuccess {
                            Log.d("GROQ_DEBUG", "Parser SUCCESS")
                            tempFilters = it
                            rateLimiter.recordCall()
                            AiResponseCache.put(query, it)
                        }.onFailure { error ->
                            Log.e("GROQ_DEBUG", "Parser FAILED: ${error.message}")
                            parseError = error
                        }

                        val currentFilters = tempFilters
                        if (currentFilters != null) {
                            filters = currentFilters
                        } else {
                            val errorMsg = parseError?.message ?: ""
                            if (errorMsg.contains("Groq API key not configured") || errorMsg.contains("Invalid Groq API key") || errorMsg.contains("not configured")) {
                                _uiState.value = AiPickUiState.Error(errorMsg)
                                historyRepository.insertPrompt(query, wasSuccessful = false)
                                return@launch
                            }
                            filters = localKeywordFallback(query)
                            usedLocalFallback = true
                            analytics.logFallbackTriggered("ai_parse_failure")
                        }
                    }
                }

                val activeFilters = filters ?: localKeywordFallback(query)
                Log.d("AiPickFlow", "AI parsed filters: $activeFilters")

                // Start progress message cycling during source aggregation
                startStatusCycling()

                // 2. Search sources
                val resultsList: List<Pair<Manga, String>>
                var failedSourcesList: List<String> = emptyList()
                val cachedResults = if (forceRefresh) null else SearchResultsCache.get(activeFilters)
                if (cachedResults != null) {
                    resultsList = cachedResults
                } else {
                    val searchResult = sourceAggregator.searchAll(activeFilters)
                    if (searchResult.isFailure) {
                        val error = searchResult.exceptionOrNull()
                        throw error ?: Exception("No sources available")
                    }

                    val searchData = searchResult.getOrThrow()
                    val searchList = searchData.mangaList
                    failedSourcesList = searchData.failedSources

                    val mappedList = searchList.map { manga ->
                        val sourceName = sourceAggregator.mangaToSourceMap[manga] ?: "Unknown"
                        Pair(manga, sourceName)
                    }

                    val deduplicated = DeduplicationEngine.deduplicate(mappedList)
                    resultsList = deduplicated

                    if (!usedLocalFallback) {
                        SearchResultsCache.put(activeFilters, deduplicated)
                    }
                }

                stopStatusCycling()

                lastResult = resultsList
                wasSuccessful = true
                retryCount = 0

                val resultSource = if (usedLocalFallback) "fallback" else "ai"
                analytics.logResultsShown(resultsList.size, resultSource)
                Log.d("AiPickFlow", "Sources queried successfully. Showing ${resultsList.size} results.")

                _uiState.value = AiPickUiState.Success(
                    mangaList = resultsList.map { AggregatedManga(it.first, it.second) },
                    aiMessage = generateAiMessage(activeFilters)
                )

            } catch (e: Exception) {
                stopStatusCycling()
                retryCount++

                // After 3 retries, permanently use local fallback for this session
                if (retryCount >= 3) {
                    permanentFallback = true
                }

                val userFriendlyMessage = when (e) {
                    is java.net.UnknownHostException, is java.io.IOException -> {
                        analytics.logError("offline")
                        "No internet connection. Check your network."
                    }
                    is retrofit2.HttpException -> {
                        if (e.code() == 429) {
                            analytics.logError("rate_limit")
                            "Too many requests. Please wait a moment."
                        } else {
                            analytics.logError("parse_error")
                            "Network API error: Code ${e.code()}"
                        }
                    }
                    is java.net.SocketTimeoutException -> {
                        analytics.logError("timeout")
                        if (lastResult?.isNotEmpty() == true) {
                            "Sources are slow. Showing partial results."
                        } else {
                            "Connection timed out. Try again."
                        }
                    }
                    else -> {
                        analytics.logError("unknown")
                        e.message ?: "An unexpected error occurred"
                    }
                }

                val currentResult = lastResult
                if (e is java.net.SocketTimeoutException && currentResult?.isNotEmpty() == true) {
                    val activeFilters = filters ?: localKeywordFallback(query)
                    _uiState.value = AiPickUiState.Success(
                        mangaList = currentResult.map { AggregatedManga(it.first, it.second) },
                        aiMessage = generateAiMessage(activeFilters)
                    )
                } else {
                    _uiState.value = AiPickUiState.Error(userFriendlyMessage)
                }
            } finally {
                // Persist prompt history whether success or failure
                if (!wasSuccessful) {
                    viewModelScope.launch {
                        historyRepository.insertPrompt(query, wasSuccessful = false)
                    }
                }
            }
        }
    }

    private suspend fun refreshQueryResultsInBackground(query: String, cachedFilters: AiStructuredFilters) {
        val searchResult = sourceAggregator.searchAll(cachedFilters)
        if (searchResult.isSuccess) {
            val searchData = searchResult.getOrThrow()
            val searchList = searchData.mangaList
            val failedSourcesList = searchData.failedSources

            val mappedList = searchList.map { manga ->
                val sourceName = sourceAggregator.mangaToSourceMap[manga] ?: "Unknown"
                Pair(manga, sourceName)
            }

            val deduplicated = DeduplicationEngine.deduplicate(mappedList)
            SearchResultsCache.put(cachedFilters, deduplicated)
            lastResult = deduplicated
            
            _uiState.value = AiPickUiState.Success(
                mangaList = deduplicated.map { AggregatedManga(it.first, it.second) },
                aiMessage = generateAiMessage(cachedFilters)
            )
            Log.d("AiPickFlow", "Silent background refresh finished. Displaying fresh results.")
        }
    }

    fun logResultClicked(position: Int, mangaSource: String) {
        analytics.logResultClicked(position, mangaSource)
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    // 5.A — Determinate progress loader with >5s warning status message
    private fun startStatusCycling() {
        stopStatusCycling()
        statusCyclingJob = viewModelScope.launch {
            val sources = listOf("Comix", "MangaFire", "MangaDex")
            var seconds = 0
            var idx = 0
            while (true) {
                if (seconds >= 5) {
                    _uiState.value = AiPickUiState.Loading("Searching ${sources.joinToString(", ")} (taking longer than usual)...")
                } else {
                    _uiState.value = AiPickUiState.Loading("Searching ${sources.joinToString(", ")}...")
                }
                delay(1000)
                seconds++
            }
        }
    }

    private fun stopStatusCycling() {
        statusCyclingJob?.cancel()
        statusCyclingJob = null
    }

    private suspend fun <T> retryWithBackoff(
        retries: Int = 3,
        initialDelay: Long = 1000,
        factor: Double = 2.0,
        block: suspend () -> Result<T>,
    ): Result<T> {
        var currentDelay = initialDelay
        var lastException: Throwable? = null
        for (attempt in 1..retries) {
            try {
                val result = block()
                if (result.isSuccess) {
                    return result
                }
                lastException = result.exceptionOrNull()
            } catch (e: Exception) {
                lastException = e
            }
            if (attempt < retries) {
                kotlinx.coroutines.delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        return Result.failure(lastException ?: Exception("Failed after $retries attempts"))
    }

    private fun localKeywordFallback(query: String): AiStructuredFilters {
        val lowercaseQuery = query.lowercase()
        val genres = mutableListOf<String>()
        val allowedGenres = listOf(
            "Action", "Romance", "Adventure", "Fantasy", "Comedy", "Drama",
            "Sci-Fi", "Mystery", "Slice of Life", "Supernatural", "Thriller",
            "Sports", "Historical", "Isekai",
        )
        for (genre in allowedGenres) {
            if (lowercaseQuery.contains(genre.lowercase())) {
                genres.add(genre)
            }
        }

        var type: String? = null
        if (lowercaseQuery.contains("manhwa")) type = "manhwa"
        else if (lowercaseQuery.contains("manga")) type = "manga"
        else if (lowercaseQuery.contains("manhua")) type = "manhua"

        var status: String? = null
        if (lowercaseQuery.contains("ongoing") || lowercaseQuery.contains("active")) status = "ongoing"
        else if (lowercaseQuery.contains("finished") || lowercaseQuery.contains("completed")) status = "finished"

        var demographic: String? = null
        if (lowercaseQuery.contains("shounen") || lowercaseQuery.contains("shonen")) demographic = "shounen"
        else if (lowercaseQuery.contains("shoujo") || lowercaseQuery.contains("shojo")) demographic = "shoujo"
        else if (lowercaseQuery.contains("seinen")) demographic = "seinen"
        else if (lowercaseQuery.contains("josei")) demographic = "josei"

        return AiStructuredFilters(
            genres = genres,
            type = type,
            status = status,
            demographic = demographic,
            minChapters = null,
            excludeTags = emptyList(),
        )
    }

    private fun generateAiMessage(filters: AiStructuredFilters): String {
        val genres = filters.genres?.joinToString(", ")?.lowercase() ?: ""
        val type = filters.type?.lowercase() ?: ""
        val status = filters.status?.lowercase() ?: ""
        
        return when {
            genres.isNotEmpty() && status.isNotEmpty() && type.isNotEmpty() ->
                "Here are some $status $genres $type picks for you ✨"
            genres.isNotEmpty() && type.isNotEmpty() ->
                "Here are some $genres $type you might enjoy 📚"
            genres.isNotEmpty() ->
                "Found some $genres picks based on your request 🔥"
            type.isNotEmpty() ->
                "Here are some $type recommendations 🎯"
            else ->
                "Here are some picks based on your request ✨"
        }
    }

    fun retry() {
        val query = lastQuery
        if (query != null) {
            sendQuery(query, forceRefresh = true)
        } else {
            _uiState.value = AiPickUiState.Idle
        }
    }

    fun clear() {
        lastQuery = null
        lastResult = null
        retryCount = 0
        permanentFallback = false
        stopStatusCycling()
        AiResponseCache.clear()
        SearchResultsCache.clear()
        _uiState.value = AiPickUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopStatusCycling()
        queryJob?.cancel()
        queryJob = null
        // 11.12 — Free memory when ViewModel is destroyed
        lastResult = null
    }
}

// VERIFICATION CHECKLIST
// □ Groq API key is not placeholder
// □ Groq API call returns valid JSON
// □ JSON parses into AiStructuredFilters without crash
// □ At least 1 of 3 sources returns results
// □ Results are deduplicated (no duplicate titles)
// □ Clicking a manga opens detail screen
// □ Back button returns to AI Pick with results preserved
// □ Rotation keeps results (ViewModel survives)
// □ Empty query shows validation error
// □ Network offline shows cached results or error
// □ Rate limit shows retry option
// □ Suggestion chips trigger search correctly
