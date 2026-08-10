package com.aman.vanish.core.network.webview

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.aman.vanish.core.network.cookies.MutableCookieJar
import kotlinx.coroutines.CancellableContinuation

class CaptchaContinuationClient(
	private val targetUrl: String,
	private val oldClearance: String?,
	private val cookieJar: MutableCookieJar,
	continuation: CancellableContinuation<Boolean>,
) : ContinuationResumeWebViewClient(continuation) {

	private var pageStartCount = 0

	override fun onPageFinished(view: WebView?, url: String?) {
		// INTENTIONALLY EMPTY
		// Do not call super — it must never resume the continuation.
	}

	override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
		super.onPageStarted(view, url, favicon)
		pageStartCount++

		// Skip the first onPageStarted (initial load).
		// Only check on subsequent navigations (redirects after CAPTCHA solve).
		if (pageStartCount <= 1) return

		// Give the cookie 500ms to be written after navigation starts.
		view?.postDelayed({
			checkClearance()
		}, 500)
	}

	private fun checkClearance() {
		// Read directly from CookieManager, NOT from OkHttp jar
		val rawCookies = CookieManager.getInstance().getCookie(targetUrl) ?: return
		val newClearance = CloudFlareHelper.parseCfClearance(rawCookies)

		// The ONLY condition to close: new cookie exists AND is different from old
		if (newClearance != null && newClearance != oldClearance) {
			// Sync all cookies to OkHttp jar for the retry
			CloudFlareHelper.syncToJar(targetUrl, rawCookies, cookieJar)

			// Resume and close
			resumeContinuation()
		}
	}
}
