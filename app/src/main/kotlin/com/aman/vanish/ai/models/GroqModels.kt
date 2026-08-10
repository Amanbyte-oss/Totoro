package com.aman.vanish.ai.models

import com.google.gson.annotations.SerializedName

data class GroqRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("temperature") val temperature: Double = 0.1,
    @SerializedName("max_tokens") val maxTokens: Int = 500,
    @SerializedName("response_format") val responseFormat: GroqResponseFormat = GroqResponseFormat()
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqResponseFormat(
    @SerializedName("type") val type: String = "json_object"
)

data class GroqResponse(
    @SerializedName("choices") val choices: List<GroqChoice>
)

data class GroqChoice(
    @SerializedName("message") val message: GroqMessage
)
