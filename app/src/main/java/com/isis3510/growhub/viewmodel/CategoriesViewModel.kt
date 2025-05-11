package com.isis3510.growhub.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isis3510.growhub.local.database.AppLocalDatabase
import com.isis3510.growhub.model.objects.Category
import com.isis3510.growhub.Repository.CategoryRepository
import com.isis3510.growhub.utils.ConnectionStatus
import com.isis3510.growhub.utils.SingleLiveEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppLocalDatabase.getDatabase(application)
    private val repository = CategoryRepository(db)

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val connectivityViewModel = ConnectivityViewModel(application)

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
        // Verificar si ya hay una carga en progreso o si se alcanzó el final
        if (currentLoadingJob?.isActive == true) {
            Log.d("CategoriesViewModel", "⛔ Skipping load - already loading")
            return
        }

        if (_hasReachedEnd.value == true && !initialLoad) {
            Log.d("CategoriesViewModel", "🛑 Reached end, triggering message")
            _showNoMoreCategoriesMessage.call()
            return
        }

        currentLoadingJob = viewModelScope.launch {
            Log.d("CategoriesViewModel", "🔄 Loading categories with offset=$offset, pageSize=$pageSize, initialLoad=$initialLoad")

            // Mostrar indicador de carga solo cuando no es la carga inicial
            if (!initialLoad) {
                _isLoadingMore.value = true
            }

            try {
                // Asegúrate de que se respete exactamente el tamaño de página
                val exactPageSize = pageSize

                // Obtener categorías según el estado de la conexión
                val newCategories : List<Category> = repository.getCategoriesOnline(exactPageSize, offset)

                Log.d("CategoriesViewModel", "📦 Loaded ${newCategories.size} new categories at offset $offset")

                if (newCategories.isNotEmpty()) {
                    // En caso de carga inicial, reemplazar la lista; de lo contrario, añadir
                    val currentList = if (initialLoad) emptyList() else _categories.value.orEmpty()
                    _categories.value = currentList + newCategories

                    // Incrementar el offset solo si recibimos exactamente el tamaño de página
                    // para asegurarnos de no saltar elementos
                    if (newCategories.size == exactPageSize) {
                        offset += exactPageSize
                        _hasReachedEnd.value = false
                        Log.d("CategoriesViewModel", "✅ More categories may be available, new offset: $offset")
                    } else {
                        // Alcanzamos el final porque recibimos menos elementos de los esperados
                        _hasReachedEnd.value = true
                        Log.d("CategoriesViewModel", "🏁 End reached: received ${newCategories.size} < $exactPageSize")

                        if (!initialLoad) {
                            _showNoMoreCategoriesMessage.call()
                        }
                    }
                } else {
                    // No se encontraron categorías nuevas
                    _hasReachedEnd.value = true
                    Log.d("CategoriesViewModel", "🏁 End reached: no new categories found")

                    // Si no fue la carga inicial y no obtuvimos nada, mostrar mensaje
                    if (!initialLoad) {
                        _showNoMoreCategoriesMessage.call()
                    }
                }
            } catch (e: Exception) {
                Log.e("CategoriesViewModel", "❌ Error loading categories: ${e.message}", e)
                // Manejo de errores
                _hasReachedEnd.value = true
                _showNoMoreCategoriesMessage.call()
            } finally {
                // Ocultar indicador de carga independientemente del resultado
                _isLoadingMore.value = false
                Log.d("CategoriesViewModel", "⏹️ Loading completed, isLoadingMore set to false")
            }
        }
    }

    fun categoriesEventualConnectivity() : String {
        return if (connectivityViewModel.networkStatus.value == ConnectionStatus.Unavailable) {
            "Please check your connection. Cannot find more categories while offline"
        } else {
            "No more categories have been found"
        }
    }

    // Función para reiniciar y volver a cargar (por ejemplo, para pull-to-refresh)
    fun resetAndLoad() {
        offset = 0
        _categories.value = emptyList()
        _hasReachedEnd.value = false
        _isLoadingMore.value = false
        currentLoadingJob?.cancel() // Cancelar cualquier carga en curso
        loadCategories(initialLoad = true)
    }
}