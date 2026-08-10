package com.aman.vanish.core.exceptions

import okhttp3.Headers
import com.aman.vanish.core.model.UnknownMangaSource
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper

class CloudFlareProtectedException(
	override val url: String,
	val mangaSource: MangaSource?,
	@Transient val headers: Headers,
) : CloudFlareException(
	source = mangaSource?.name ?: "Unknown",
	blockedUrl = url,
	message = "Protected by CloudFlare"
) {
	override val state: Int get() = CloudFlareHelper.PROTECTION_CAPTCHA
	val sourceObj: MangaSource = mangaSource ?: UnknownMangaSource
}
