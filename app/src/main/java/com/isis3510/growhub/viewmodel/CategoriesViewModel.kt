package com.isis3510.growhub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.Repository.CategoryRepository
import com.isis3510.growhub.utils.SingleLiveEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppLocalDatabase.getDatabase(application)
    private val repository = CategoryRepository(db)

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // --- Enhancement States ---
    private val _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _hasReachedEnd = MutableLiveData<Boolean>(false)
    val hasReachedEnd: LiveData<Boolean> = _hasReachedEnd

    // Use a SingleLiveEvent to ensure the message is shown only once per trigger
    private val _showNoMoreCategoriesMessage = SingleLiveEvent<Unit>()
    val showNoMoreCategoriesMessage: LiveData<Unit> = _showNoMoreCategoriesMessage
    // --- End Enhancement States ---


    // Paging variables
    private var offset: Int = 0
    private val pageSize: Int = 5
    private var currentLoadingJob: Job? = null // To prevent concurrent loads

    init {
        // Perform initial load
        loadCategories(initialLoad = true)
        // Schedule deletion of old categories (can be done less frequently if needed)
        viewModelScope.launch {
            repository.deleteOlderCategories()
        }
    }

    fun loadCategories(initialLoad: Boolean = false) {
        // Prevent multiple simultaneous loads and loading if the end is reached
        if (currentLoadingJob?.isActive == true || _hasReachedEnd.value == true) {
            return
        }

        currentLoadingJob = viewModelScope.launch {
            // Show loading indicator only when loading *more* items, not initial load
            if (!initialLoad) {
                _isLoadingMore.value = true
            }

            try {
                // Obtain categories based on the paging we defined
                val newCategories = repository.getCategories(pageSize, offset)

                if (newCategories.isNotEmpty()) {
                    val currentList = _categories.value.orEmpty()
                    _categories.value = currentList + newCategories // Append new items

                    // Increases the offset only if we got the full page size we expected
                    if (newCategories.size == pageSize) {
                        offset += pageSize
                        _hasReachedEnd.value = false // We might have more
                    } else {
                        // Reached the end because we received fewer items than the page size
                        _hasReachedEnd.value = true
                    }
                } else {
                    // Reached the end because the repository returned an empty list
                    _hasReachedEnd.value = true
                    // If this wasn't the initial load and we got nothing, trigger the message
                    if (!initialLoad) {
                        _showNoMoreCategoriesMessage.call() // Signal the event
                    }
                }
            } catch (e: Exception) {
                // Handle potential errors (e.g., network, database)
                // You might want to expose an error state here
                _hasReachedEnd.value = true // Stop trying if there was an error
                _showNoMoreCategoriesMessage.call() // Signal the event to inform user
            } finally {
                // Hide loading indicator regardless of outcome
                _isLoadingMore.value = false
            }
        }
    }

    // Optional: Add a function to reset the state if needed (e.g., for pull-to-refresh)
    fun resetAndLoad() {
        offset = 0
        _categories.value = emptyList()
        _hasReachedEnd.value = false
        _isLoadingMore.value = false
        currentLoadingJob?.cancel() // Cancel any ongoing load
        loadCategories(initialLoad = true)
    }
}
