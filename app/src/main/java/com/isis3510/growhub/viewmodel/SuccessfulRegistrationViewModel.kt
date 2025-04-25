package com.isis3510.growhub.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Event
import kotlinx.coroutines.launch

/**
 * Created by: Juan Manuel Jáuregui
 */

@RequiresApi(Build.VERSION_CODES.O)
class SuccessfulRegistrationViewModel (
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade(),
    private val eventID: String
) : ViewModel() {

    val registeredEvent = mutableStateListOf<Event>()

    init {
        loadRegisteredEventsFromFirebase()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadRegisteredEventsFromFirebase() {
        viewModelScope.launch {
            val registeredEvent = firebaseFacade.fetchRegistrationData(eventID)
            this@SuccessfulRegistrationViewModel.registeredEvent.add(registeredEvent)
        }
    }
}