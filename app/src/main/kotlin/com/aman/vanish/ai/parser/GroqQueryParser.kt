package com.aman.vanish.ai.parser

import android.util.Log
import com.aman.vanish.ai.AiConfig
import com.aman.vanish.ai.models.AiStructuredFilters
import com.aman.vanish.ai.models.GroqMessage
import com.aman.vanish.ai.models.GroqRequest
import com.aman.vanish.ai.network.GroqRetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class GroqQueryParser {

    private val apiService = GroqRetrofitClient.apiService
    private val gson = Gson()

    companion object {
        private const val TAG = "AiPickGroq"
    }

    suspend fun parseUserQuery(text: String): Result<AiStructuredFilters> {
        Log.d("GROQ_DEBUG", "=== parseUserQuery CALLED ===")
        Log.d("GROQ_DEBUG", "Input text: $text")
        Log.d("GROQ_DEBUG", "API Key starts with: ${AiConfig.GROQ_API_KEY.take(6)}")

        val trimmedText = text.trim()
        // 9.B & 2. — Input Sanitization and Empty check
        if (trimmedText.isEmpty()) {
            Log.e(TAG, "Empty or whitespace-only query submitted")
            return Result.failure(IllegalArgumentException("Please enter a query"))
        }

        // Guard: if key is still placeholder, fail fast with clear message
        if (AiConfig.GROQ_API_KEY == "gsk-your-groq-api-key-here") {
            Log.e("GROQ_DEBUG", "KEY IS PLACEHOLDER - ABORTING")
            Log.e(TAG, "GROQ_API_KEY is not set in AiConfig.kt")
            return Result.failure(IllegalStateException("Groq API key not configured"))
        }

        val systemPrompt = buildSystemPrompt()

        // Strip HTML tags and limit to 200 characters max
        val sanitizedText = trimmedText
            .replace(Regex("<[^>]*>"), "")
            .take(200)

        // Block obvious prompt injection patterns
        val lowerText = sanitizedText.lowercase()
        if (lowerText.contains("ignore previous instructions") ||
            lowerText.contains("system prompt") ||
            lowerText.contains("you are now") ||
            lowerText.contains("<system>") ||
            lowerText.contains("<prompt>")
        ) {
            Log.w(TAG, "Potential prompt injection detected: $sanitizedText")
            return Result.failure(IllegalArgumentException("Invalid query content"))
        }

        val request = GroqRequest(
            model = AiConfig.GROQ_MODEL,
            messages = listOf(
                GroqMessage(role = "system", content = systemPrompt),
                GroqMessage(role = "user", content = sanitizedText)
            )
        )

        return try {
            Log.d("GROQ_DEBUG", "About to call Groq API...")
            if (com.aman.vanish.BuildConfig.DEBUG) {
                Log.d(TAG, "Sending request to Groq: model=${request.model}")
            }
            
            // 2. — Retry logic for 429 or 503
            val response = try {
                apiService.createChatCompletion(request)
            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                if (code == 429 || code == 503) {
                    Log.w(TAG, "Groq returned HTTP $code. Retrying once after 2 seconds...")
                    kotlinx.coroutines.delay(2000)
                    apiService.createChatCompletion(request)
                } else {
                    throw e
                }
            }
            Log.d("GROQ_DEBUG", "Groq response received, choices count: ${response.choices?.size ?: 0}")

            val choices = response.choices
            val content = if (choices.isNullOrEmpty()) {
                Log.w(TAG, "Groq returned empty choices array. Falling back to local keyword parsing.")
                val fallback = localKeywordFallback(sanitizedText)
                Log.d(TAG, "Local fallback parsed filters: $fallback")
                return Result.success(fallback)
            } else {
                choices.firstOrNull()?.message?.content
            } ?: throw IllegalStateException("Empty response from Groq")

            if (com.aman.vanish.BuildConfig.DEBUG) {
                Log.d(TAG, "Groq raw response: $content")
            }

            val filters = try {
                gson.fromJson(content.trim(), AiStructuredFilters::class.java)
                    ?: throw JsonSyntaxException("Parsed null object")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON parse error, falling back to keyword parsing: ${e.message}")
                val fallback = localKeywordFallback(sanitizedText)
                Log.d(TAG, "Local fallback parsed filters: $fallback")
                return Result.success(fallback)
            }

            if (com.aman.vanish.BuildConfig.DEBUG) {
                Log.d(TAG, "Parsed filters: $filters")
            }
            Result.success(filters)

        } catch (e: retrofit2.HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string() ?: "Unknown HTTP error"
            Log.e(TAG, "HTTP $code: $errorBody")
            Log.e("GROQ_DEBUG", "Groq call FAILED: ${e.javaClass.simpleName}: ${e.message}")
            when (code) {
                401 -> Result.failure(IllegalStateException("Invalid Groq API key"))
                429 -> Result.failure(IllegalStateException("Rate limit hit. Wait a moment."))
                else -> Result.failure(IllegalStateException("Groq error $code: $errorBody"))
            }
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Network error: ${e.message}")
            Log.e("GROQ_DEBUG", "Groq call FAILED: ${e.javaClass.simpleName}: ${e.message}")
            Result.failure(IllegalStateException("Network error. Check your connection."))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            Log.e("GROQ_DEBUG", "Groq call FAILED: ${e.javaClass.simpleName}: ${e.message}")
            Result.failure(IllegalStateException("Unexpected error: ${e.message}"))
        }
    }

    private fun localKeywordFallback(query: String): AiStructuredFilters {
        val lowercaseQuery = query.lowercase()
        val genres = mutableListOf<String>()
        val allowedGenres = listOf(
            "Action", "Romance", "Adventure", "Fantasy", "Comedy", "Drama",
            "Sci-Fi", "Mystery", "Slice of Life", "Supernatural", "Thriller",
            "Sports", "Historical", "Isekai"
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
            excludeTags = emptyList()
        )
    }

    private fun buildSystemPrompt(): String {
        return """
You are a manga recommendation filter parser. The user will describe what manga they want.
You must ONLY output a valid JSON object. Do not add markdown, explanations, or conversation.
Allowed genres: Action, Adventure, Comedy, Drama, Fantasy, Horror, Isekai, Martial Arts, Mystery, Psychological, Romance, Sci-Fi, Slice of Life, Sports, Supernatural, Thriller.
Allowed types: manga, manhwa, manhua.
Allowed status: ongoing, finished.

Output format:
{
  "genres": ["Action", "Romance"],
  "type": "manhwa",
  "status": "ongoing",
  "demographic": null,
  "minChapters": null,
  "excludeTags": []
}

Rules:
- If the user mentions a genre not in the allowed list, map it to the closest allowed genre or omit it.
- If the query is vague, return broad filters with nulls.
- Never recommend specific manga titles.
- Never browse the internet.
- Output ONLY the JSON object, nothing else.
        """.trimIndent()
    }
}
