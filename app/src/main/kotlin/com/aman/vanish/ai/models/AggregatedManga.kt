package com.aman.vanish.ai.models

import com.google.gson.annotations.SerializedName
import org.koitharu.kotatsu.parsers.model.Manga

data class AggregatedManga(
    @SerializedName("manga") val manga: Manga,
    @SerializedName("sourceName") val sourceName: String,
    @SerializedName("isDuplicate") val isDuplicate: Boolean = false
)
