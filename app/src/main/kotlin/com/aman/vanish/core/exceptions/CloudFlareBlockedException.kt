package com.aman.vanish.core.exceptions

import com.aman.vanish.core.model.UnknownMangaSource
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper

class CloudFlareBlockedException(
	override val url: String,
	val mangaSource: MangaSource?,
) : CloudFlareException(
	source = mangaSource?.name ?: "Unknown",
	blockedUrl = url,
	message = "Blocked by CloudFlare"
) {
	override val state: Int get() = CloudFlareHelper.PROTECTION_BLOCKED
	val sourceObj: MangaSource = mangaSource ?: UnknownMangaSource
}
