package com.example.mimotts.network

import com.example.mimotts.data.CloneVoiceRequest
import com.example.mimotts.data.CloneVoiceResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

interface TtsApi {
    @POST("/api/tts")
    @Streaming
    suspend fun generateSpeech(@Body request: TtsRequest): ResponseBody

    @POST("/api/clone-voice")
    suspend fun cloneVoice(@Body request: CloneVoiceRequest): CloneVoiceResponse

    @GET("/api/voices")
    suspend fun getVoices(): List<VoiceDto>
}

data class TtsRequest(
    val text: String,
    val voice: String = "zh-CN-XiaoxiaoNeural",
    val rate: String = "+0%",
    val pitch: String = "+0Hz",
    val volume: String = "+0%",
    val cloneVoiceId: String? = null,
)

data class VoiceDto(
    val name: String,
    val shortName: String,
    val gender: String,
    val locale: String,
)
