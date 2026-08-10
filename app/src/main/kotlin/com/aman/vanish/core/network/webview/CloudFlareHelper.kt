package com.aman.vanish.core.network.webview

import android.webkit.CookieManager
import com.aman.vanish.core.network.cookies.MutableCookieJar
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object CloudFlareHelper {

	/**
	 * Reads the current `cf_clearance` cookie value for the given URL
	 * directly from Android's [CookieManager].
	 *
	 * This must be used instead of the OkHttp jar because the WebView
	 * writes cookies to [CookieManager], and there can be a sync delay
	 * before they appear in the OkHttp jar.
	 */
	fun getClearanceCookie(url: String): String? {
		val rawCookies = CookieManager.getInstance().getCookie(url) ?: return null
		return parseCfClearance(rawCookies)
	}

	/**
	 * Parses a raw cookie string (semicolon-delimited) for the value
	 * of the `cf_clearance` cookie.
	 */
	fun parseCfClearance(cookieString: String?): String? {
		if (cookieString.isNullOrBlank()) return null
		return cookieString.split(";").map { it.trim() }
			.find { it.startsWith("cf_clearance=") }
			?.substringAfter("cf_clearance=")
			?.trim()
	}

	/**
	 * Syncs all cookies from a raw cookie string into the OkHttp [MutableCookieJar].
	 * Call this after detecting a new `cf_clearance` so that the subsequent
	 * OkHttp retry request has the solved cookie.
	 */
	fun syncToJar(url: String, rawCookieString: String, cookieJar: MutableCookieJar) {
		val httpUrl = url.toHttpUrlOrNull() ?: return
		rawCookieString.split(";").forEach { raw ->
			Cookie.parse(httpUrl, raw.trim())?.let { cookie ->
				cookieJar.saveFromResponse(httpUrl, listOf(cookie))
			}
		}
	}
}
