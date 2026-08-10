package com.aman.vanish.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.aman.vanish.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A helper class to manage Firebase Analytics event logging and user properties.
 */
@Singleton
class AnalyticsHelper @Inject constructor(
	@ApplicationContext private val context: Context
) {
	private val firebaseAnalytics: FirebaseAnalytics by lazy {
		FirebaseAnalytics.getInstance(context)
	}

	private var isDebugAnalyticsEnabled = false

	init {
		// Disable analytics collection in debug builds by default, unless explicitly enabled
		setAnalyticsCollectionEnabled(!BuildConfig.DEBUG || isDebugAnalyticsEnabled)
	}

	/**
	 * Toggles Firebase Analytics collection enabled/disabled.
	 */
	fun setAnalyticsCollectionEnabled(enabled: Boolean) {
		firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
	}

	/**
	 * Debug mode toggle for development builds to test logging.
	 */
	fun setDebugAnalyticsEnabled(enabled: Boolean) {
		isDebugAnalyticsEnabled = enabled
		setAnalyticsCollectionEnabled(!BuildConfig.DEBUG || enabled)
	}

	/**
	 * Log a screen view event manually.
	 *
	 * @param screenName The name of the screen.
	 * @param activityName The class name of the activity/fragment.
	 */
	fun logScreenView(screenName: String, activityName: String) {
		val bundle = Bundle().apply {
			putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
			putString(FirebaseAnalytics.Param.SCREEN_CLASS, activityName)
		}
		firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
	}

	/**
	 * Log custom events with a Bundle of parameters.
	 */
	fun logEvent(eventName: String, params: Bundle? = null) {
		firebaseAnalytics.logEvent(eventName, params)
	}

	/**
	 * Log a simple string key-value event.
	 */
	fun logEvent(eventName: String, key: String, value: String) {
		val bundle = Bundle().apply {
			putString(key, value)
		}
		firebaseAnalytics.logEvent(eventName, bundle)
	}

	/**
	 * Log a button click event.
	 */
	fun logButtonClick(buttonId: String, screenName: String) {
		val bundle = Bundle().apply {
			putString("button_id", buttonId)
			putString("screen_name", screenName)
		}
		firebaseAnalytics.logEvent("button_click", bundle)
	}

	/**
	 * Log a user login event with the sign-in method.
	 */
	fun logLogin(method: String) {
		val bundle = Bundle().apply {
			putString(FirebaseAnalytics.Param.METHOD, method)
		}
		firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
	}

	/**
	 * Log a user purchase event.
	 *
	 * @param itemId The product identifier (e.g. premium subscription SKU).
	 * @param value The value of the purchase.
	 * @param currency The ISO 4217 currency code (e.g., "USD").
	 */
	fun logPurchase(itemId: String, value: Double, currency: String) {
		val bundle = Bundle().apply {
			putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
			putDouble(FirebaseAnalytics.Param.VALUE, value)
			putString(FirebaseAnalytics.Param.CURRENCY, currency)
		}
		firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)
	}

	/**
	 * Set a custom user property.
	 */
	fun setUserProperty(name: String, value: String?) {
		firebaseAnalytics.setUserProperty(name, value)
	}

	/**
	 * Set premium status user property.
	 */
	fun setUserPremiumStatus(isPremium: Boolean) {
		setUserProperty("user_premium_status", isPremium.toString())
	}
}
