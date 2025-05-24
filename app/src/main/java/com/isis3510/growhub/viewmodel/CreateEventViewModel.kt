package com.isis3510.growhub.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.Repository.CreateEventRepository
import com.isis3510.growhub.offline.NetworkUtils
import com.isis3510.growhub.offline.OfflineEventManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateEventViewModel(
    private val createEventRepository: CreateEventRepository,
    private val context: Context
) : ViewModel() {

    private val offlineManager = OfflineEventManager(context, createEventRepository)

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _cost = MutableStateFlow("")
    val cost: StateFlow<String> = _cost

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _startDate = MutableStateFlow("26/02/2025")
    val startDate: StateFlow<String> = _startDate

    private val _endDate = MutableStateFlow("26/02/2025")
    val endDate: StateFlow<String> = _endDate

    private val _startHour = MutableStateFlow("9:00 AM")
    val startHour: StateFlow<String> = _startHour

    private val _endHour = MutableStateFlow("10:00 AM")
    val endHour: StateFlow<String> = _endHour

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address

    // New state variables for address validation
    private val _addressValidated = MutableStateFlow(false)
    val addressValidated: StateFlow<Boolean> = _addressValidated

    private val _addressError = MutableStateFlow<String?>(null)
    val addressError: StateFlow<String?> = _addressError

    private val _formattedAddress = MutableStateFlow<String?>(null)
    val formattedAddress: StateFlow<String?> = _formattedAddress

    // Geographic coordinates for the validated address
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude

    // Address validation in progress indicator
    private val _validatingAddress = MutableStateFlow(false)
    val validatingAddress: StateFlow<Boolean> = _validatingAddress

    private val _details = MutableStateFlow("")
    val details: StateFlow<String> = _details

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl

    private val _locationId = MutableStateFlow("/locations/j5XQsX5v0ln9FGWXd4v5")
    val locationId: StateFlow<String> = _locationId

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city

    private val _isUniversity = MutableStateFlow(false)
    val isUniversity: StateFlow<Boolean> = _isUniversity

    private val _allSkills = MutableStateFlow<List<String>>(emptyList())
    val allSkills: StateFlow<List<String>> = _allSkills

    private val _selectedSkills = MutableStateFlow<List<String>>(emptyList())
    val selectedSkills: StateFlow<List<String>> = _selectedSkills

    private val _skillSelectionError = MutableStateFlow<String?>(null)
    val skillSelectionError: StateFlow<String?> = _skillSelectionError

    private val skillNameToId: MutableMap<String, String> = mutableMapOf()

    private val categoryNameToId: MutableMap<String, String> = mutableMapOf()

    private val _allCategories = MutableStateFlow<List<String>>(emptyList())
    val allCategories : StateFlow<List<String>> = _allCategories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _eventCreated = MutableStateFlow<Boolean?>(null)
    val eventCreated: StateFlow<Boolean?> = _eventCreated

    private val _createdEventName = MutableStateFlow<String?>(null)
    val createdEventName: StateFlow<String?> = _createdEventName

    init {
        fetchSkillsAndCategories()
        syncOfflineEventsIfPossible()
    }

    fun resetEventCreated() {
        _eventCreated.value = null
        _createdEventName.value = null
    }

    fun onNameChange(value: String) { _name.value = value }
    fun onCostChange(value: String) { _cost.value = value }
    fun onCategoryChange(value: String) { _category.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onStartDateChange(value: String) { _startDate.value = value }
    fun onEndDateChange(value: String) { _endDate.value = value }
    fun onStartHourChange(value: String) { _startHour.value = value }
    fun onEndHourChange(value: String) { _endHour.value = value }

    // Updated address change function to reset validation
    fun onAddressChange(value: String) {
        _address.value = value
        _addressValidated.value = false
        _formattedAddress.value = null
        _latitude.value = null
        _longitude.value = null
        _addressError.value = null
    }

    fun onDetailsChange(value: String) { _details.value = value }
    fun onImageUrlChange(value: String) { _imageUrl.value = value }
    fun onLocationIdChange(value: String) { _locationId.value = value }
    fun onCityChange(value: String) { _city.value = value }
    fun onIsUniversityChange(value: Boolean) { _isUniversity.value = value }

    // New function to validate address with Google Maps API
    fun validateAddress() {
        if (_address.value.isBlank()) {
            _addressError.value = "Please enter an address."
            return
        }

        viewModelScope.launch {
            delay(500)

            _validatingAddress.value = true
            _addressError.value = null

            try {
                val result = createEventRepository.validateAndGeocodeAddress(_address.value)

                if (result.isValid) {
                    _addressValidated.value = true
                    _formattedAddress.value = result.formattedAddress
                    _latitude.value = result.latitude
                    _longitude.value = result.longitude

                    // Auto-update the address field with the formatted version
                    _address.value = result.formattedAddress ?: _address.value
                } else {
                    _addressValidated.value = false
                    _formattedAddress.value = null
                    _latitude.value = null
                    _longitude.value = null
                    _addressError.value = result.errorMessage ?: "Invalid address"
                }
            } catch (e: Exception) {
                _addressValidated.value = false
                _addressError.value = "Error validating address: ${e.message}"
            } finally {
                _validatingAddress.value = false
            }
        }
    }

    fun toggleSkill(skillName: String) {
        if (_selectedSkills.value.contains(skillName)) {
            _selectedSkills.value = _selectedSkills.value - skillName
            _skillSelectionError.value = null
            return
        }

        if (_selectedSkills.value.size >= 3) {
            _skillSelectionError.value = "You can select up to 3 skills only."
            return
        }

        _selectedSkills.value = _selectedSkills.value + skillName
        _skillSelectionError.value = null
    }

    private fun fetchSkillsAndCategories() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val db = FirebaseFirestore.getInstance()

            /* skills -------------------------------------------------------- */
            val skillDocs = db.collection("skills").get().await()
            val skillsTmp = mutableListOf<String>()
            for (doc in skillDocs.documents) {
                val name = doc.getString("name") ?: continue
                skillsTmp += name
                skillNameToId[name] = doc.id
            }
            _allSkills.value = skillsTmp

            /* categories ---------------------------------------------------- */
            val catDocs = db.collection("categories").get().await()
            val catTmp  = mutableListOf<String>()
            for (doc in catDocs.documents) {
                val name = doc.getString("name") ?: continue
                catTmp += name
                categoryNameToId[name] = doc.id
            }
            _allCategories.value = catTmp

        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
        } finally {
            _isLoading.value = false
        }
    }

    private fun parseStartDateTime(): Timestamp {
        val pattern = "dd/MM/yyyy hh:mm a"
        val dateTimeString = "${_startDate.value} ${_startHour.value}"
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val parsedDate: Date = sdf.parse(dateTimeString) ?: Date()
            Timestamp(parsedDate)
        } catch (e: Exception) {
            Timestamp.now()
        }
    }

    private fun parseEndDateTime(): Timestamp {
        val pattern = "dd/MM/yyyy hh:mm a"
        val dateTimeString = "${_endDate.value} ${_endHour.value}"
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val parsedDate: Date = sdf.parse(dateTimeString) ?: Date()
            Timestamp(parsedDate)
        } catch (e: Exception) {
            Timestamp.now()
        }
    }

    fun createEvent() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val eventCost = _cost.value.toIntOrNull() ?: 0
                val startTimestamp = parseStartDateTime()
                val endTimestamp = parseEndDateTime()

                val selectedSkillIds = _selectedSkills.value.mapNotNull { skillNameToId[it] }

                val hasInternet = NetworkUtils.isNetworkAvailable(context)
                if (!hasInternet) {
                    // Guardar offline
                    offlineManager.saveOfflineEvent(
                        name = _name.value,
                        cost = eventCost,
                        category = _category.value,
                        description = _description.value,
                        startDate = startTimestamp,
                        endDate = endTimestamp,
                        locationId = _locationId.value,
                        imageUrl = _imageUrl.value,
                        address = _address.value,
                        details = _details.value,
                        city = _city.value,
                        isUniversity = _isUniversity.value,
                        skillIds = selectedSkillIds,
                        latitude = _latitude.value,
                        longitude = _longitude.value
                    )
                    _eventCreated.value = true
                    _errorMessage.value = "No internet connection. Your event will be uploaded automatically once you're back online."
                } else {
                    val success = offlineManager.uploadSingleEvent(
                        name = _name.value,
                        cost = eventCost,
                        category = _category.value,
                        description = _description.value,
                        startDate = startTimestamp,
                        endDate = endTimestamp,
                        locationId = _locationId.value,
                        imageUrl = _imageUrl.value,
                        address = _address.value,
                        details = _details.value,
                        city = _city.value,
                        isUniversity = _isUniversity.value,
                        skillIds = selectedSkillIds,
                        latitude = _latitude.value,
                        longitude = _longitude.value
                    )
                    _eventCreated.value = success.isNullOrEmpty()
                    _createdEventName.value = _name.value
                    if (!success.isNullOrEmpty()) {
                        clearForm()
                    } else {
                        _errorMessage.value = "Could not create event. Please try again."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncOfflineEventsIfPossible() {
        viewModelScope.launch {
            val hasInternet = NetworkUtils.isNetworkAvailable(context)
            if (hasInternet) {
                val uploadedCount = offlineManager.tryUploadAllOfflineEvents()
                if (uploadedCount > 0) {
                    _errorMessage.value = "You have synced $uploadedCount event(s) that were pending."
                }
            }
        }
    }

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
        _city.value = ""
        _isUniversity.value = false
        _selectedSkills.value = emptyList()
        _skillSelectionError.value = null
        _addressValidated.value = false
        _addressError.value = null
        _formattedAddress.value = null
        _latitude.value = null
        _longitude.value = null
    }
}
