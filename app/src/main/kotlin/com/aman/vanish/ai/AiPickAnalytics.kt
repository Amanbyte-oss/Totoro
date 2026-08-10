package com.aman.vanish.ai

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Structured analytics for the AI Pick feature.
 * Currently logs to Logcat under tag "AiPick.Analytics".
 * Designed for easy migration to Firebase Analytics later —
 * each method maps 1-to-1 to a Firebase event call.
 */
@Singleton
class AiPickAnalytics @Inject constructor() {

    private companion object {
        const val TAG = "AiPick.Analytics"
    }

    /** Called when user submits a prompt. Logs length only — no PII. */
    fun logPromptSent(promptLength: Int) {
        Log.i(TAG, "event=ai_prompt_sent prompt_length=$promptLength")
    }

    /** Called when results are displayed. */
    fun logResultsShown(count: Int, source: String) {
        Log.i(TAG, "event=ai_results_shown count=$count source=$source")
    }

    /** Called when a manga card is tapped. */
    fun logResultClicked(position: Int, mangaSource: String) {
        Log.i(TAG, "event=ai_result_clicked position=$position manga_source=$mangaSource")
    }

    /** Called when an error occurs. Type: timeout / rate_limit / parse_error / offline */
    fun logError(type: String) {
        Log.i(TAG, "event=ai_error type=$type")
    }

    /** Called when the AI API fails and keyword fallback is triggered. */
    fun logFallbackTriggered(reason: String) {
        Log.i(TAG, "event=ai_fallback_triggered reason=$reason")
    }

    /** Called when a rate limit block is hit. */
    fun logRateLimitHit() {
        Log.i(TAG, "event=ai_rate_limit_hit")
    }

    /** Called when a cached result is served instantly. */
    fun logCacheHit(promptLength: Int) {
        Log.i(TAG, "event=ai_cache_hit prompt_length=$promptLength")
    }
}
