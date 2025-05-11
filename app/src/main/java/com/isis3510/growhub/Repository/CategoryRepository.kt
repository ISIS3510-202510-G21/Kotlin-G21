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
        android.util.Log.d("CategoryRepository", "Getting categories from local DB with limit=$limit, offset=$offset")

        // Obtener categorías de la base de datos local usando los parámetros de paginación
        val localEntities = categoryDao.getCategories(limit, offset)
        android.util.Log.d("CategoryRepository", "Local DB returned ${localEntities.size} categories")

        // Convertir entidades a objetos de dominio
        return localEntities.map { it.toCategory() }
    }

    // Query to Firebase, store group in local database and return its list
    private suspend fun fetchCategoriesFromFirebaseAndStore(limit: Int, offset: Int): List<Category> {
        // Log the operation for debugging
        android.util.Log.d("CategoryRepository", "Fetching categories from Firebase with limit=$limit, offset=$offset")

        // Return all categories from Firebase
        val allCategories = FirebaseServicesFacade().fetchCategories()
        android.util.Log.d("CategoryRepository", "Firebase returned ${allCategories.size} categories in total")

        // Verificar que tenemos suficientes categorías para el offset solicitado
        if (offset >= allCategories.size) {
            android.util.Log.d("CategoryRepository", "Offset $offset is beyond available categories (${allCategories.size})")
            return emptyList()
        }

        // Realizar la paginación de manera correcta
        val paginatedCategories = allCategories.drop(offset).take(limit)
        android.util.Log.d("CategoryRepository", "Paginated to ${paginatedCategories.size} categories")

        if (paginatedCategories.isNotEmpty()) {
            val entities = paginatedCategories.map { it.toEntity() }
            // Guardar las categorías en la base de datos local
            categoryDao.insertCategories(entities)
            android.util.Log.d("CategoryRepository", "Saved ${entities.size} categories to local database")
        }

        return paginatedCategories
    }

    // Caching then fallback to Network
    suspend fun getCategoriesOnline(limit: Int, offset: Int): List<Category> {
        val localCategories = getCategoriesLocal(limit, offset)
        return localCategories.ifEmpty {
            fetchCategoriesFromFirebaseAndStore(limit, offset)
        }
    }

    // Delete categories that are older than 2 days
    suspend fun deleteOlderCategories() {
        return categoryDao.deleteOlderThan(
            now = System.currentTimeMillis(),
            maxAge = 2 * 24 * 60 * 60 * 1000
        )
    }
}