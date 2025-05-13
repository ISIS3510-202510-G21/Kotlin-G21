package com.isis3510.growhub.view.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.R
import com.isis3510.growhub.viewmodel.CreateEventViewModel
import com.isis3510.growhub.viewmodel.CreateEventViewModelFactory
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
fun PreviewCreateEventContent() {
    var nameError by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }
    var startHourError by remember { mutableStateOf<String?>(null) }
    var endHourError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var detailsError by remember { mutableStateOf<String?>(null) }

    CreateEventContentInternal(
        name = "Sample Event",
        cost = "20",
        category = "Music",
        description = "Sample",
        startDate = "01/01/2025",
        endDate = "01/01/2025",
        startHour = "10:00 AM",
        endHour = "12:00 PM",
        address = "Test 123",
        details = "Bring snacks",
        nameError = nameError,
        costError = costError,
        categoryError = categoryError,
        descriptionError = descriptionError,
        startDateError = startDateError,
        endDateError = endDateError,
        startHourError = startHourError,
        endHourError = endHourError,
        addressError = addressError,
        detailsError = detailsError,
        onNameChange = { nameError = null },
        onCostChange = { costError = null },
        onCategoryChange = { categoryError = null },
        onDescriptionChange = { descriptionError = null },
        onStartDateChange = { startDateError = null },
        onEndDateChange = { endDateError = null },
        onStartHourChange = { startHourError = null },
        onEndHourChange = { endHourError = null },
        onAddressChange = { addressError = null },
        onDetailsChange = { detailsError = null },
        onImageUpload = {},
        onCreateEvent = {},
        onNavigateBack = {},
        skillSelectionError = "error de skills selector"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventView(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val factory = remember { CreateEventViewModelFactory(context) }
    val viewModel: CreateEventViewModel = viewModel(factory = factory)

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val eventCreated by viewModel.eventCreated.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onImageUrlChange(uri.toString())
        }
    }

    LaunchedEffect(eventCreated) {
        if (eventCreated == true) {
            onNavigateBack()
            viewModel.resetEventCreated()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                CreateEventContent(
                    onNavigateBack = onNavigateBack,
                    viewModel = viewModel,
                    onImagePickerClick = { imagePickerLauncher.launch("image/*") }
                )
            }
        }

        LaunchedEffect(errorMessage) {
            errorMessage?.let { msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }
}

@Composable
fun CreateEventContent(
    onNavigateBack: () -> Unit,
    viewModel: CreateEventViewModel,
    onImagePickerClick: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val cost by viewModel.cost.collectAsState()
    val category by viewModel.category.collectAsState()
    val description by viewModel.description.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val startHour by viewModel.startHour.collectAsState()
    val endHour by viewModel.endHour.collectAsState()
    val address by viewModel.address.collectAsState()
    val details by viewModel.details.collectAsState()
    val city by viewModel.city.collectAsState()
    val isUniversity by viewModel.isUniversity.collectAsState()
    val allSkills by viewModel.allSkills.collectAsState()
    val selectedSkills by viewModel.selectedSkills.collectAsState()
    val skillSelectionError by viewModel.skillSelectionError.collectAsState()

    var nameError by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }
    var startHourError by remember { mutableStateOf<String?>(null) }
    var endHourError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var detailsError by remember { mutableStateOf<String?>(null) }

    CreateEventContentInternal(
        name = name,
        cost = cost,
        category = category,
        description = description,
        startDate = startDate,
        endDate = endDate,
        startHour = startHour,
        endHour = endHour,
        address = address,
        details = details,
        nameError = nameError,
        costError = costError,
        categoryError = categoryError,
        descriptionError = descriptionError,
        startDateError = startDateError,
        endDateError = endDateError,
        startHourError = startHourError,
        endHourError = endHourError,
        addressError = addressError,
        detailsError = detailsError,
        skillSelectionError = skillSelectionError,
        onNameChange = {
            viewModel.onNameChange(it)
            nameError = null
            if (it.isBlank()) nameError = "Please enter the event name."
        },
        onCostChange = {
            viewModel.onCostChange(it)
            costError = null
            if (it.isBlank()) {
                costError = "Please enter a cost."
            } else if (it.toDoubleOrNull() == null) {
                costError = "Cost must be a valid number."
            }
        },
        onCategoryChange = {
            viewModel.onCategoryChange(it)
            categoryError = null
            if (it.isBlank()) categoryError = "Please select a category."
        },
        onDescriptionChange = {
            viewModel.onDescriptionChange(it)
            descriptionError = null
            if (it.isBlank()) descriptionError = "Description cannot be empty."
        },
        onStartDateChange = {
            viewModel.onStartDateChange(it)
            startDateError = null
            if (it.isBlank()) startDateError = "Please enter a start date."
        },
        onEndDateChange = {
            viewModel.onEndDateChange(it)
            endDateError = null
            if (it.isBlank()) endDateError = "Please enter an end date."
        },
        onStartHourChange = {
            viewModel.onStartHourChange(it)
            startHourError = null
            if (it.isBlank()) startHourError = "Please enter a start hour."
        },
        onEndHourChange = {
            viewModel.onEndHourChange(it)
            endHourError = null
            if (it.isBlank()) endHourError = "Please enter an end hour."
        },
        onAddressChange = {
            viewModel.onAddressChange(it)
            addressError = null
            if (it.isBlank()) addressError = "Please enter an address."
        },
        onDetailsChange = {
            viewModel.onDetailsChange(it)
            detailsError = null
            if (it.isBlank()) detailsError = "Please enter some details."
        },
        onImageUpload = { onImagePickerClick() },
        onCreateEvent = {
            var hasError = false
            if (name.isBlank()) {
                nameError = "Please enter the event name."
                hasError = true
            }
            if (cost.isBlank()) {
                costError = "Please enter a cost."
                hasError = true
            } else if (cost.toDoubleOrNull() == null) {
                costError = "Cost must be a valid number."
                hasError = true
            }
            if (category.isBlank()) {
                categoryError = "Please select a category."
                hasError = true
            }
            if (description.isBlank()) {
                descriptionError = "Description cannot be empty."
                hasError = true
            }
            if (startDate.isBlank()) {
                startDateError = "Please enter a start date."
                hasError = true
            }
            if (endDate.isBlank()) {
                endDateError = "Please enter an end date."
                hasError = true
            }
            if (startHour.isBlank()) {
                startHourError = "Please enter a start hour."
                hasError = true
            }
            if (endHour.isBlank()) {
                endHourError = "Please enter an end hour."
                hasError = true
            }
            if (address.isBlank()) {
                addressError = "Please enter an address."
                hasError = true
            }
            if (details.isBlank()) {
                detailsError = "Please enter some details."
                hasError = true
            }
            if (city.isBlank()) {
                // O si quieres un error local, p.e. cityError
                hasError = true
            }
            if (selectedSkills.size > 3) {
                hasError = true
            }

            if (!hasError) {
                viewModel.createEvent()
                // onNavigateBack()
            }
        },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun CreateEventContentInternal(
    name: String,
    cost: String,
    category: String,
    description: String,
    startDate: String,
    endDate: String,
    startHour: String,
    endHour: String,
    address: String,
    details: String,
    nameError: String?,
    costError: String?,
    categoryError: String?,
    descriptionError: String?,
    startDateError: String?,
    endDateError: String?,
    startHourError: String?,
    endHourError: String?,
    addressError: String?,
    detailsError: String?,
    skillSelectionError: String?,
    onNameChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onStartHourChange: (String) -> Unit,
    onEndHourChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onImageUpload: () -> Unit,
    onCreateEvent: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "",
                    tint = Color.Black,
                    modifier = Modifier.size(29.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Event",
                fontSize = 25.sp,
                color = Color.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .padding(vertical = 0.dp)
        ) {
            LabeledTextField(
                label = "Name",
                value = name,
                placeholder = "Write the name of the event",
                isError = (nameError != null),
                onValueChange = onNameChange
            )
            ErrorText(nameError)

            LabeledTextField(
                label = "Cost",
                value = cost,
                placeholder = "Write the cost of your event",
                isError = (costError != null),
                onValueChange = onCostChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            ErrorText(costError)

            Text(
                text = "Category",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 1.dp)
            )
            CategoryDropdown(
                selectedCategory = category,
                placeholderText = "Choose the category of your event",
                onCategorySelected = onCategoryChange,
                isError = (categoryError != null)
            )
            ErrorText(categoryError)

            LabeledTextField(
                label = "Description",
                value = description,
                placeholder = "Write the description of your event ...",
                isError = (descriptionError != null),
                onValueChange = onDescriptionChange
            )
            ErrorText(descriptionError)

            Text(
                text = "Date",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 1.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "From", fontSize = 12.sp, color = Color.Black)
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = onStartDateChange,
                        placeholder = { Text("DD/MM/YYYY", fontSize = 12.sp) },
                        isError = (startDateError != null),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = "",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    ErrorText(startDateError)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "To", fontSize = 12.sp, color = Color.Black)
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = onEndDateChange,
                        placeholder = { Text("DD/MM/YYYY", fontSize = 12.sp) },
                        isError = (endDateError != null),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = "",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    ErrorText(endDateError)
                }
            }

            Text(
                text = "Hour",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 1.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "From", fontSize = 12.sp, color = Color.Black)
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = onStartHourChange,
                        placeholder = { Text("HH:MM AM", fontSize = 12.sp) },
                        isError = (startHourError != null),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clock),
                                contentDescription = "",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    ErrorText(startHourError)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "To", fontSize = 12.sp, color = Color.Black)
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = onEndHourChange,
                        placeholder = { Text("HH:MM AM", fontSize = 12.sp) },
                        isError = (endHourError != null),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clock),
                                contentDescription = "",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    ErrorText(endHourError)
                }
            }

            LabeledTextField(
                label = "Address",
                value = address,
                placeholder = "Write the address of your event",
                isError = (addressError != null),
                onValueChange = onAddressChange
            )
            ErrorText(addressError)

            Text(
                text = "City",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 1.dp)
            )
            CityDropdown()

            Text(
                text = "This is an university event?",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 1.dp)
            )
            UniversityDropdown()

            LabeledTextField(
                label = "Details",
                value = details,
                placeholder = "Write the details of your event",
                isError = (detailsError != null),
                onValueChange = onDetailsChange
            )
            ErrorText(detailsError)

            Text(
                text = "Skills (max 3)",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 1.dp)
            )
            SkillsSelection()
            ErrorText(skillSelectionError)

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onImageUpload,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Image of the event", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5669FF))
            ) {
                Text(
                    text = "Create Event",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ErrorText(error: String?) {
    error?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDropdown(viewModel: CreateEventViewModel = viewModel(factory = CreateEventViewModelFactory(LocalContext.current))) {
    val city by viewModel.city.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val cities = listOf(
        "Bogotá",
        "Medellín",
        "Cali",
        "Barranquilla",
        "Cartagena",
        "Bucaramanga",
        "Pereira",
        "Manizales",
        "Santa Marta",
        "Ibagué"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = city,
            onValueChange = {},
            placeholder = { Text("Choose a city") },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(50.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cities.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c) },
                    onClick = {
                        viewModel.onCityChange(c)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityDropdown(viewModel: CreateEventViewModel = viewModel(factory = CreateEventViewModelFactory(LocalContext.current))) {
    val isUniversity by viewModel.isUniversity.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val boolOptions = listOf("true", "false")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (isUniversity) "true" else "false",
            onValueChange = {},
            placeholder = { Text("Is it a university event?") },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(50.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            boolOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        viewModel.onIsUniversityChange(option == "true")
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsSelection(
    viewModel: CreateEventViewModel = viewModel(
        factory = CreateEventViewModelFactory(LocalContext.current)
    )
) {
    val allSkills by viewModel.allSkills.collectAsState()
    val selectedSkills by viewModel.selectedSkills.collectAsState()

    /* Contenedor con altura fija y scroll interno */
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth()
        ) {
            /* Usamos FlowRow para que los chips se distribuyan en filas */
            FlowRow(
                // Compose Foundation
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp)
            ) {
                allSkills.forEach { skill ->
                    val isSelected = selectedSkills.contains(skill)

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleSkill(skill) },
                        label = { Text(text = skill, maxLines = 1) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) Color(0xFF5669FF).copy(alpha = .15f)
                            else Color(0xFFF5F5F5),
                            labelColor = if (isSelected) Color(0xFF5669FF) else Color.Black
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    placeholder: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(top = 5.dp, bottom = 1.dp)
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        isError = isError,
        keyboardOptions = keyboardOptions,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    placeholderText: String,
    onCategorySelected: (String) -> Unit,
    isError: Boolean,
    viewModel: CreateEventViewModel = viewModel(
        factory = CreateEventViewModelFactory(LocalContext.current)
    )
) {
    /* La lista ahora viene de Firestore vía ViewModel ----------- */
    val categories by viewModel.allCategories.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            placeholder = { Text(placeholderText) },
            isError = isError,
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(50.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) },
                    onClick = {
                        onCategorySelected(cat)
                        expanded = false
                    }
                )
            }
        }
    }
}
