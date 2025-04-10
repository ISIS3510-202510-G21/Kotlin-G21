package com.isis3510.growhub.service

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by: Juan Manuel Jáuregui
 */

interface GeminiApiService {

    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun sendMessage(@Body request: GeminiRequest): GeminiResponse

    companion object {
        fun create(): GeminiApiService {
            val apiKey = "A"

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val originalUrl = originalRequest.url

                    val newUrl = originalUrl.newBuilder()
                        .addQueryParameter("key", apiKey)
                        .build()

                    val newRequest = originalRequest.newBuilder()
                        .url(newUrl)
                        .build()

                    chain.proceed(newRequest)
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(GeminiApiService::class.java)
        }
    }
}

data class Message(
    val role: String,
    val content: String
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent
)





