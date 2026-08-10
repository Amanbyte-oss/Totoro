package com.aman.vanish.ai

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guards the Groq API daily quota using SharedPreferences.
 * Key = "ai_calls_YYYY-MM-DD", resets automatically at midnight by using today's date in the key.
 * Limit is set to 1,500 to leave a safety margin.
 */
@Singleton
class AIRateLimiter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val DAILY_LIMIT = 1500
        const val WARN_THRESHOLD = 1400
        private const val PREFS_NAME = "ai_rate_limiter"
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _remainingQuota = MutableStateFlow(getRemainingQuota())
    val remainingQuota: StateFlow<Int> = _remainingQuota.asStateFlow()

    private fun todayKey(): String = "ai_calls_${LocalDate.now().format(DATE_FORMATTER)}"

    private fun getTodayCount(): Int = prefs.getInt(todayKey(), 0)

    fun getRemainingQuota(): Int = maxOf(0, DAILY_LIMIT - getTodayCount())

    /** Returns true if the API call is allowed, false if daily limit is reached. */
    fun canMakeCall(): Boolean = getTodayCount() < WARN_THRESHOLD

    /** Records one API call for today. Call this after a successful Groq API request. */
    fun recordCall() {
        val key = todayKey()
        val newCount = getTodayCount() + 1
        prefs.edit().putInt(key, newCount).apply()
        _remainingQuota.value = maxOf(0, DAILY_LIMIT - newCount)
    }

    /** Returns a human-readable quota string for UI display. */
    fun getQuotaBadgeText(): String {
        val remaining = getRemainingQuota()
        return "AI: $remaining remaining"
    }
}
