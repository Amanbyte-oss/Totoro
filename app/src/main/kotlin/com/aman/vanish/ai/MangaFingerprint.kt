package com.aman.vanish.ai

import org.koitharu.kotatsu.parsers.model.Manga

data class MangaFingerprint(
    val normalizedTitle: String,
    val author: String?
) {
    companion object {
        fun create(manga: Manga): MangaFingerprint {
            val titleNorm = TitleNormalizer.normalize(manga.title)
            val authorsStr = manga.authors.joinToString(", ")
            val authorNorm = if (authorsStr.isNotEmpty()) TitleNormalizer.normalize(authorsStr) else null
            return MangaFingerprint(titleNorm, authorNorm)
        }
    }
}
