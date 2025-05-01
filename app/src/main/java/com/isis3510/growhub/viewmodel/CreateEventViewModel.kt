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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateEventViewModel(
    private val createEventRepository: CreateEventRepository = CreateEventRepository(),
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

    init {
        fetchSkillsAndCategories()
        syncOfflineEventsIfPossible()
    }

    fun resetEventCreated() { _eventCreated.value = null }
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
    fun onCityChange(value: String) { _city.value = value }
    fun onIsUniversityChange(value: Boolean) { _isUniversity.value = value }

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
                val eventCost = _cost.value.toDoubleOrNull() ?: 0.0
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
                        skillIds = selectedSkillIds
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
                        skillIds = selectedSkillIds
                    )
                    _eventCreated.value = success
                    if (success) {
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
    }
}
