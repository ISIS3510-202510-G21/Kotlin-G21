package com.isis3510.growhub.Repository

import android.util.Log
import com.isis3510.growhub.local.data.CategoryDao
import com.isis3510.growhub.local.data.toDomainModel
import com.isis3510.growhub.local.data.toEntity
import com.isis3510.growhub.model.facade.FirebaseServicesFacade
import com.isis3510.growhub.model.objects.Category
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

// Re-define or import DataResult if not globally available
sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val exception: Exception) : DataResult<Nothing>()
    object Loading : DataResult<Nothing>()
}

// Concrete class directly, no interface implementation
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val firebaseFacade: FirebaseServicesFacade,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private var isNetworkFetchInProgress = false

    // Method signature remains the same, just remove 'override'
    fun getPaginatedCategories(page: Int, pageSize: Int): Flow<DataResult<List<Category>>> = flow {
        emit(DataResult.Loading)
        val offset = (page - 1) * pageSize
        try {
            // 1. Emit local data
            val localCategories = categoryDao.getCategories(limit = pageSize, offset = offset)
            emit(DataResult.Success(localCategories.map { it.toDomainModel() }))

            // 2. Trigger network check/fetch on first page load
            if (page == 1 && !isNetworkFetchInProgress) {
                Log.d("CategoryRepository", "Triggering network check/fetch.")
                fetchAndStoreAllCategories()
            }
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Error getting paginated categories", e)
            emit(DataResult.Error(e))
        }
    }.flowOn(ioDispatcher)

    private suspend fun fetchAndStoreAllCategories() {
        if (isNetworkFetchInProgress) return
        isNetworkFetchInProgress = true
        withContext(ioDispatcher) {
            try {
                Log.d("CategoryRepository", "Fetching all categories from Firebase...")
                val networkCategories = firebaseFacade.fetchCategories()
                Log.d("CategoryRepository", "Fetched ${networkCategories.size} categories from Firebase.")
                val categoryEntities = networkCategories.mapIndexed { index, category ->
                    category.toEntity(order = index)
                }
                categoryDao.deleteAll()
                categoryDao.insertAll(categoryEntities)
                Log.d("CategoryRepository", "Stored categories locally.")
            } catch (e: Exception) {
                Log.e("CategoryRepository", "Failed to fetch/store categories", e)
            } finally {
                isNetworkFetchInProgress = false
            }
        }
    }

    // Method signature remains the same, just remove 'override'
    suspend fun refreshCategoriesFromNetwork() {
        Log.d("CategoryRepository", "Explicit refresh requested.")
        fetchAndStoreAllCategories()
    }

    // Method signature remains the same, just remove 'override'
    suspend fun getTotalCategoryCount(): Int {
        return withContext(ioDispatcher) {
            categoryDao.getCategoryCount()
        }
    }
}