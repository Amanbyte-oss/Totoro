package com.aman.vanish.core.network.webview

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

abstract class ContinuationResumeWebViewClient(
	private val continuation: CancellableContinuation<Boolean>,
) : WebViewClient() {

	protected fun resumeContinuation() {
		if (continuation.isActive) {
			continuation.resume(true)
		}
	}

	override fun onReceivedError(
		view: WebView?,
		request: WebResourceRequest?,
		error: WebResourceError?,
	) {
		super.onReceivedError(view, request, error)
		if (request?.isForMainFrame == true && continuation.isActive) {
			continuation.resume(false)
		}
	}
}
