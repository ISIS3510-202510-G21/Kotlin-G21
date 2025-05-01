package com.isis3510.growhub.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.isis3510.growhub.Repository.CreateEventRepository

class CreateEventViewModelFactory(
    private val appContext: Context,
    private val createEventRepository: CreateEventRepository = CreateEventRepository()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateEventViewModel::class.java)) {
            return CreateEventViewModel(
                createEventRepository = createEventRepository,
                context = appContext
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
    }
}
