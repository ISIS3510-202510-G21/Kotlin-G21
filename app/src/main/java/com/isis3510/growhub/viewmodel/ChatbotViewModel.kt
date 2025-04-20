package com.isis3510.growhub.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.growhub.service.GeminiApiService
import com.isis3510.growhub.service.GeminiContent
import com.isis3510.growhub.service.GeminiPart
import com.isis3510.growhub.service.GeminiRequest
import com.isis3510.growhub.service.Message
import kotlinx.coroutines.launch

/**
 * Created by: Juan Manuel Jáuregui
 */

class ChatbotViewModel : ViewModel() {

    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages
    var isBotActive = mutableStateOf(true)
    private val geminiService = GeminiApiService.create()

    fun sendMessage(message: String, firebaseAnalytics: FirebaseAnalytics) {
        Log.d("ChatbotViewModel", "message: $message")

        _messages.add(Message(role = "user", content = message))

        val bundle = Bundle().apply {
            putString("user_message", message)
        }
        firebaseAnalytics.logEvent("chatbot_message_sent", bundle)

        viewModelScope.launch {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(message))
                        )
                    )
                )

                val response = geminiService.sendMessage(request)
                val botMessage = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text ?: "No response from Gemini"

                Log.d("ChatbotViewModel", "Response: $botMessage")

                _messages.add(Message(role = "assistant", content = botMessage))

            } catch (e: Exception) {
                Log.d("ChatbotViewModel", "Error: ${e.message}")
                _messages.add(Message(role = "assistant", content = "Error: ${e.message}"))
            }
        }
    }

    fun checkBotStatus() {
        viewModelScope.launch {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart("¿Estás activo?"))
                        )
                    )
                )
                val response = geminiService.sendMessage(request)
                isBotActive.value = true
                Log.d("ChatbotViewModel", "Bot is active: $response")
            } catch (e: Exception) {
                isBotActive.value = false
                Log.d("ChatbotViewModel", "Error checking bot status: ${e.message}")
            }
        }
    }

    fun sendInitialBotMessage() {
        if (_messages.isEmpty()) {
            _messages.add(Message(role = "assistant", content = "Hello, how can I help you today?"))
        }
    }
}

