package com.isis3510.growhub.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Category
import kotlinx.coroutines.launch

class CategoriesViewModel(
    // Consider using Dependency Injection
    private val firebaseFacade: FirebaseServicesFacade = FirebaseServicesFacade()
) : ViewModel() {

    val categories = mutableStateListOf<Category>()
    // Consider using StateFlow here as well for Compose:
    // private val _categories = MutableStateFlow<List<Category>>(emptyList())
    // val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val fetchedCategories = firebaseFacade.fetchCategories()
                categories.clear() // Ensure list is fresh
                categories.addAll(fetchedCategories)
                // If using StateFlow: _categories.value = fetchedCategories
            } catch (e: Exception) {
                // Handle exceptions during Firebase fetch
                // Log.e("CategoriesViewModel", "Error fetching categories", e)
                // Expose error state if needed
            }
        }
    }

    // Potential future function for refreshing data
    // fun refreshCategories() {
    //     loadCategories()
    // }
}