package com.isis3510.growhub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.isis3510.growhub.repository.CreateEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel que contiene el estado del formulario y la lógica para crear el evento.
 */
class CreateEventViewModel(
    private val createEventRepository: CreateEventRepository = CreateEventRepository()
) : ViewModel() {

    // Estado de cada campo del formulario
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _cost = MutableStateFlow("")
    val cost: StateFlow<String> = _cost

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _startDate = MutableStateFlow("26/02/2025") // valor por defecto
    val startDate: StateFlow<String> = _startDate

    private val _endDate = MutableStateFlow("26/02/2025") // valor por defecto
    val endDate: StateFlow<String> = _endDate

    private val _startHour = MutableStateFlow("9:00 AM")
    val startHour: StateFlow<String> = _startHour

    private val _endHour = MutableStateFlow("10:00 AM")
    val endHour: StateFlow<String> = _endHour

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address

    private val _details = MutableStateFlow("")
    val details: StateFlow<String> = _details

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl

    // Ejemplo de un locationId, puede venir de otra parte
    private val _locationId = MutableStateFlow("/locations/j5XQsX5v0ln9FGWXd4v5")
    val locationId: StateFlow<String> = _locationId

    // Para controlar loading y error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Funciones para actualizar el estado
    fun onNameChange(value: String) { _name.value = value }
    fun onCostChange(value: String) { _cost.value = value }
    fun onCategoryChange(value: String) { _category.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onStartDateChange(value: String) { _startDate.value = value }
    fun onEndDateChange(value: String) { _endDate.value = value }
    fun onStartHourChange(value: String) { _startHour.value = value }
    fun onEndHourChange(value: String) { _endHour.value = value }
    fun onAddressChange(value: String) { _address.value = value }
    fun onDetailsChange(value: String) { _details.value = value }
    fun onImageUrlChange(value: String) { _imageUrl.value = value }
    fun onLocationIdChange(value: String) { _locationId.value = value }

    /**
     * Convierte _startDate y _startHour en un Timestamp final.
     * Ej: "26/02/2025" + "9:00 AM" => Timestamp
     */
    private fun parseStartDateTime(): Timestamp {
        // Ajustar el patrón a tu conveniencia: dd/MM/yyyy HH:mm
        val pattern = "dd/MM/yyyy hh:mm a" // 26/02/2025 9:00 AM
        val dateTimeString = "${_startDate.value} ${_startHour.value}"

        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val parsedDate: Date = sdf.parse(dateTimeString) ?: Date()
            Timestamp(parsedDate)
        } catch (e: Exception) {
            // Si falla el parseo, usar la fecha actual
            Timestamp.now()
        }
    }

    /**
     * Llama al repositorio para guardar en Firestore.
     */
    fun createEvent() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val endDate = parseStartDateTime()
                val startTimestamp = parseStartDateTime()
                val eventCost = _cost.value.toDoubleOrNull() ?: 0.0

                createEventRepository.createEvent(
                    name = _name.value,
                    cost = eventCost,
                    category = _category.value,
                    description = _description.value,
                    startDate = startTimestamp,
                    endDate = endDate,
                    locationId = _locationId.value,
                    imageUrl = _imageUrl.value,
                    address = _address.value,
                    details = _details.value
                )

                // Si se crea exitosamente, podríamos limpiar campos o mostrar feedback
                clearForm()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia los campos del formulario si se desea.
     */
    private fun clearForm() {
        _name.value = ""
        _cost.value = ""
        _category.value = ""
        _description.value = ""
        _startDate.value = ""
        _endDate.value = ""
        _startHour.value = ""
        _endHour.value = ""
        _address.value = ""
        _details.value = ""
        _imageUrl.value = null
    }
}
