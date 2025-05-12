package com.isis3510.growhub.viewmodel

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.isis3510.growhub.Repository.CategoryRepository
import com.isis3510.growhub.Repository.EventRepository
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.Event
import com.isis3510.growhub.service.GeminiApiService
import com.isis3510.growhub.service.GeminiContent
import com.isis3510.growhub.service.GeminiPart
import com.isis3510.growhub.service.GeminiRequest
import com.isis3510.growhub.service.Message
import com.isis3510.growhub.utils.ConnectionStatus
import kotlinx.coroutines.launch

/**
 * Created by: Juan Manuel Jáuregui
 */

class ChatbotViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages
    var isBotActive = mutableStateOf(true)
    private val geminiService = GeminiApiService.create()
    private var selectedCategory: String? = null
    private val db = AppLocalDatabase.getDatabase(application)
    private val eventRepository = EventRepository(db)
    private val categoryRepository = CategoryRepository(db)

    private val localEvents = mutableStateListOf<Event>()
    private val localCategories = mutableStateListOf<Category>()

    private val connectivityViewModel = ConnectivityViewModel(application)

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
            viewModelScope.launch {
                val isOnline = connectivityViewModel.networkStatus.value == ConnectionStatus.Available
                val greeting = if (isOnline) {
                    "Hello, how can I help you today?"
                } else {
                    "You're offline. Choose one:\n1. About GrowHub\n2. Show stored events\n3. Show event categories"
                }
                _messages.add(Message(role = "assistant", content = greeting))
            }
        }
    }


    private val categoryLetterMap = mutableMapOf<String, String>()

    fun handleOfflineInput(message: String) {
        _messages.add(Message(role = "user", content = message))

        viewModelScope.launch {
            val categories = categoryRepository.getCategoriesOnline(limit = 10, offset = 0)
                .distinctBy { it.name }
            localCategories.clear()
            localCategories.addAll(categories)

            val events = eventRepository.getEvents(limit = 10, offset = 0).distinctBy { it.name }
            localEvents.clear()
            localEvents.addAll(events)
        }

        when (message.trim().lowercase()) {
            "1" -> {
                _messages.add(
                    Message(
                        role = "assistant", content =
                        "GrowHub is an event platform where you can explore paid and free events by category and book a spot instantly."
                    )
                )
            }

            "2" -> {
                val eventsNames = localEvents.map { it.name }.distinct()
                val eventsText = if (eventsNames.isEmpty()) "No events stored locally." else
                    "Stored events:\n" + eventsNames.joinToString("\n• ", prefix = "• ")
                _messages.add(Message(role = "assistant", content = eventsText))
            }

            "3" -> {
                val categories = localCategories.distinctBy { it.name }
                categoryLetterMap.clear()

                val letters = ('a'..'z').toList()
                val listText = categories.mapIndexed { index, cat ->
                    val letter = letters.getOrNull(index)?.toString() ?: (index + 1).toString()
                    categoryLetterMap[letter] = cat.name
                    "$letter. ${cat.name}"
                }.joinToString("\n")

                val response = if (categories.isEmpty()) "No categories stored locally."
                else "Select a category by typing its letter:\n$listText"

                _messages.add(Message(role = "assistant", content = response))
            }

            else -> {
                val categoryName = categoryLetterMap[message.lowercase()]
                if (categoryName != null) {
                    val eventsInCategory = localEvents
                        .filter { it.category == categoryName }
                        .map { it.name }

                    val response = if (eventsInCategory.isEmpty()) {
                        "No events found in $categoryName."
                    } else {
                        "Events in $categoryName:\n" + eventsInCategory.joinToString(
                            "\n• ",
                            prefix = "• "
                        )
                    }
                    _messages.add(Message(role = "assistant", content = response))
                    categoryLetterMap.clear() // Clear map after use
                } else {
                    _messages.add(
                        Message(
                            role = "assistant",
                            content = "Please select 1, 2, or 3. Or pick a valid letter if choosing a category."
                        )
                    )
                }
            }
        }
    }
}

