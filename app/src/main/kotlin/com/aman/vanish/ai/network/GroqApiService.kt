package com.aman.vanish.ai.network

import com.aman.vanish.ai.models.GroqRequest
import com.aman.vanish.ai.models.GroqResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface GroqApiService {

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: GroqRequest
    ): GroqResponse
}
