package com.aman.vanish.core.network.cookies

import android.webkit.CookieManager
import androidx.annotation.WorkerThread
import androidx.core.util.Predicate
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidCookieJar : MutableCookieJar {

	private val cookieManager = CookieManager.getInstance()
	private val store = ConcurrentHashMap<String, List<Cookie>>()

	@WorkerThread
	override fun loadForRequest(url: HttpUrl): List<Cookie> {
		val host = url.host
		val cookies = store[host] ?: emptyList()
		val rawCookie = cookieManager.getCookie(url.toString()) ?: ""
		val managerCookies = rawCookie.split(';').mapNotNull {
			Cookie.parse(url, it.trim())
		}
		return (cookies + managerCookies).associateBy { it.name }.values.toList()
	}

	@WorkerThread
	override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
		if (cookies.isEmpty()) {
			return
		}
		val host = url.host
		val existing = store[host] ?: emptyList()
		val updated = (existing + cookies).associateBy { it.name }.values.toList()
		store[host] = updated

		val urlString = url.toString()
		for (cookie in cookies) {
			cookieManager.setCookie(urlString, cookie.toString())
		}
	}

	override fun removeCookies(url: HttpUrl, predicate: Predicate<Cookie>?) {
		val host = url.host
		val cookies = store[host] ?: emptyList()
		val filtered = cookies.filter { c ->
			if (predicate != null && predicate.test(c)) {
				val urlString = url.toString()
				val nc = c.newBuilder()
					.expiresAt(System.currentTimeMillis() - 100000)
					.build()
				cookieManager.setCookie(urlString, nc.toString())
				false
			} else {
				true
			}
		}
		if (filtered.isEmpty()) {
			store.remove(host)
		} else {
			store[host] = filtered
		}
	}

	override suspend fun clear() = suspendCoroutine<Boolean> { continuation ->
		store.clear()
		cookieManager.removeAllCookies(continuation::resume)
	}
}
