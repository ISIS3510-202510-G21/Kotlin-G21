package com.isis3510.growhub.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Category
// Import the CONCRETE repository class
import com.isis3510.growhub.Repository.CategoryRepository
// Import DataResult (if defined in Repository file or elsewhere)
import com.isis3510.growhub.Repository.DataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI State data class remains the same
data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val totalItems: Int = 0
)

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    // --- Dependency Setup ---
    private val categoryDao = AppLocalDatabase.getDatabase(application).categoryDao()
    private val firebaseFacade = FirebaseServicesFacade()
    private val categoryRepository: CategoryRepository = CategoryRepository(categoryDao, firebaseFacade)

    private val _uiState = MutableStateFlow(CategoriesUiState(isLoading = true))
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    // Number of categories to be fetched on one try
    private val pageSize = 5

    init {
        Log.d("CategoriesViewModel", "ViewModel initialized. Loading initial categories.")
        loadCategories(page = 1)
    }

    private fun loadCategories(page: Int, loadMore: Boolean = false) {
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore) {
            Log.d("CategoriesViewModel", "Load request ignored, already loading.")
            return
        }
        Log.d("CategoriesViewModel", "Loading categories for page: $page")

        _uiState.update {
            it.copy(
                isLoading = page == 1 && !loadMore,
                isLoadingMore = loadMore,
                error = null
            )
        }

        viewModelScope.launch {
            // Fetch total count first
            val totalCount = categoryRepository.getTotalCategoryCount() // Call concrete repo method
            Log.d("CategoriesViewModel", "Total category count from repo: $totalCount")

            // Fetch paginated data
            categoryRepository.getPaginatedCategories(page, pageSize) // Call concrete repo method
                .catch { e ->
                    Log.e("CategoriesViewModel", "Error collecting categories flow", e)
                    _uiState.update {
                        it.copy(isLoading = false, isLoadingMore = false, error = e.localizedMessage ?: "Failed to load categories")
                    }
                }
                .collectLatest { result ->
                    when (result) {
                        is DataResult.Success -> {
                            val newCategories = result.data
                            Log.d("CategoriesViewModel", "Received ${newCategories.size} categories for page $page.")
                            _uiState.update { currentState ->
                                val updatedList = if (loadMore) currentState.categories + newCategories else newCategories
                                val canLoadMore = updatedList.size < totalCount || (page == 1 && totalCount == 0 && newCategories.isNotEmpty())
                                Log.d("CategoriesViewModel", "Updating state. New list size: ${updatedList.size}, Can load more: $canLoadMore")
                                currentState.copy(
                                    isLoading = false,
                                    isLoadingMore = false,
                                    categories = updatedList,
                                    currentPage = page,
                                    canLoadMore = canLoadMore,
                                    totalItems = totalCount
                                )
                            }
                        }
                        is DataResult.Error -> {
                            Log.e("CategoriesViewModel", "Error result received: ${result.exception.message}")
                            _uiState.update {
                                it.copy(isLoading = false, isLoadingMore = false, error = result.exception.localizedMessage ?: "An error occurred")
                            }
                        }
                        is DataResult.Loading -> {
                            Log.d("CategoriesViewModel", "DataResult.Loading received.")
                            // Loading state already handled at the start of the function
                        }
                    }
                }
        }
    }

    fun loadMoreCategories() {
        val currentState = _uiState.value
        if (!currentState.isLoadingMore && currentState.canLoadMore) {
            Log.d("CategoriesViewModel", "Requesting load more. Current page: ${currentState.currentPage}")
            loadCategories(page = currentState.currentPage + 1, loadMore = true)
        } else {
            Log.d("CategoriesViewModel", "Cannot load more. isLoadingMore: ${currentState.isLoadingMore}, canLoadMore: ${currentState.canLoadMore}")
        }
    }

    fun refreshCategories() {
        Log.d("CategoriesViewModel", "Manual refresh requested.")
        _uiState.update { it.copy(isLoading = true, error = null, currentPage = 1, canLoadMore = true) }
        viewModelScope.launch {
            try {
                categoryRepository.refreshCategoriesFromNetwork() // Call concrete repo method
                loadCategories(page = 1)
            } catch (e: Exception) {
                Log.e("CategoriesViewModel", "Error during manual refresh", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Refresh failed") }
            }
        }
    }
}