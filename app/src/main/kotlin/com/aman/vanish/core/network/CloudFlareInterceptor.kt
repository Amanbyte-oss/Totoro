package com.aman.vanish.core.network

import okhttp3.Interceptor
import okhttp3.Response
import com.aman.vanish.core.exceptions.CloudFlareException
import com.aman.vanish.core.exceptions.CloudFlareProtectedException
import com.aman.vanish.core.exceptions.CloudFlareBlockedException
import org.koitharu.kotatsu.parsers.model.MangaSource

class CloudFlareInterceptor : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val response = chain.proceed(request)

		if (response.code == 403 || response.code == 503) {
			val cfRay = response.header("cf-ray") != null
			val serverCloudflare = response.header("server")?.contains("cloudflare", ignoreCase = true) == true
			
			// Inspect response body without permanently consuming it (using peekBody)
			val bodyString = runCatching {
				response.peekBody(1024 * 1024).string()
			}.getOrDefault("")

			val bodyContainsMarkers = bodyString.contains("cdn-cgi/challenge-platform") ||
					bodyString.contains("turnstile") ||
					bodyString.contains("__cf_chl_jschl_tk__") ||
					bodyString.contains("cf-turnstile")

			if (cfRay || serverCloudflare || bodyContainsMarkers) {
				val sourceTag = request.tag(MangaSource::class.java)
				val blockedUrl = request.url.toString()
				
				response.close()
				if (bodyContainsMarkers || cfRay) {
					throw CloudFlareProtectedException(
						url = blockedUrl,
						mangaSource = sourceTag,
						headers = response.headers
					)
				} else {
					throw CloudFlareBlockedException(
						url = blockedUrl,
						mangaSource = sourceTag
					)
				}
			}
		}

		return response
	}
}
