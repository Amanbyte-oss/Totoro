package com.aman.vanish.favourites.domain.model

import com.aman.vanish.core.model.MangaSource

data class Cover(
	val url: String?,
	val source: String,
) {
	val mangaSource by lazy { MangaSource(source) }
}
