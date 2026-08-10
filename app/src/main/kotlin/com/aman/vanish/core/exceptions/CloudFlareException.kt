package com.aman.vanish.core.exceptions

import okhttp3.Response
import java.io.IOException
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper

open class CloudFlareException(
	val source: String,
	val blockedUrl: String,
	val originalResponse: Response? = null,
	message: String = "Cloudflare challenge required for $source",
) : IOException(message) {
	open val url: String get() = blockedUrl
	open val state: Int get() = CloudFlareHelper.PROTECTION_CAPTCHA
}
