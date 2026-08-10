package com.aman.vanish.core.exceptions.resolve

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

object CaptchaResultBus {
	private val continuations = ConcurrentHashMap<String, CancellableContinuation<Boolean>>()

	val blockedSources = MutableStateFlow<Map<String, CaptchaEvent>>(emptyMap())
	val events = MutableSharedFlow<CaptchaEvent>(extraBufferCapacity = 64)

	data class CaptchaEvent(
		val url: String,
		val sourceName: String,
		val userAgent: String?
	)

	suspend fun awaitResult(url: String, sourceName: String, userAgent: String?, onLaunch: () -> Unit): Boolean {
		return suspendCancellableCoroutine { continuation ->
			val event = CaptchaEvent(url, sourceName, userAgent)
			blockedSources.value = blockedSources.value + (sourceName to event)
			continuations[url] = continuation
			continuation.invokeOnCancellation {
				continuations.remove(url)
			}
			onLaunch()
		}
	}

	fun emitResult(url: String, solved: Boolean) {
		continuations.remove(url)?.resume(solved)
	}

	fun clearBlocked(sourceName: String) {
		blockedSources.value = blockedSources.value - sourceName
	}
}
