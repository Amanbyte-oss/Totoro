package com.aman.vanish.ai.models

import com.google.gson.annotations.SerializedName

data class AiUserQuery(
    @SerializedName("rawText") val rawText: String
)
