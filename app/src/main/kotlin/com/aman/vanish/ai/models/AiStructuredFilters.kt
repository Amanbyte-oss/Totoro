package com.aman.vanish.ai.models

import com.google.gson.annotations.SerializedName

data class AiStructuredFilters(
    @SerializedName("genres") val genres: List<String>,
    @SerializedName("type") val type: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("demographic") val demographic: String?,
    @SerializedName("minChapters") val minChapters: Int?,
    @SerializedName("excludeTags") val excludeTags: List<String>?
)
