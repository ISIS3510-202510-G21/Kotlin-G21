package com.isis3510.growhub.Repository

import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.model.objects.toCategory
import com.isis3510.growhub.model.objects.toEntity
import com.isis3510.growhub.model.facade.FirebaseServicesFacade

class CategoryRepository(db: AppLocalDatabase) {

    private val categoryDao = db.categoryDao()

    // Query categories from local database using paging to split in groups of 5
    private suspend fun getCategoriesLocal(limit: Int, offset: Int): List<Category> {
        return categoryDao.getCategories(limit, offset).map { it.toCategory() }
    }

    // Query to Firebase, store group in local database and return its list
    private suspend fun fetchCategoriesFromFirebaseAndStore(limit: Int, offset: Int): List<Category> {
        // Return all categories from Firebase
        val allCategories = FirebaseServicesFacade().fetchCategories()
        // Perform paging process in local memory
        val paginatedCategories = allCategories.drop(offset).take(limit)

        if (paginatedCategories.isNotEmpty()) {
            val entities = paginatedCategories.map { it.toEntity() }
            categoryDao.insertCategories(entities)
        }
        return paginatedCategories
    }

    // Caching then fallback to Network
    suspend fun getCategories(limit: Int, offset: Int): List<Category> {
        val localCategories = getCategoriesLocal(limit, offset)
        return localCategories.ifEmpty {
            fetchCategoriesFromFirebaseAndStore(limit, offset)
        }
    }

    // Delete categories that are older than 7 days
    suspend fun deleteOlderCategories() {
        return categoryDao.deleteOlderThan(
            now = System.currentTimeMillis(),
            maxAge = 7 * 24 * 60 * 60 * 1000
        )
    }
}