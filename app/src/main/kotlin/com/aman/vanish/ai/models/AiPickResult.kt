package com.aman.vanish.ai.models

import com.google.gson.annotations.SerializedName

data class AiPickResult(
    @SerializedName("filters") val filters: AiStructuredFilters,
    @SerializedName("confidence") val confidence: String
)
