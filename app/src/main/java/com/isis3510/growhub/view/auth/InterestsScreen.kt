package com.isis3510.growhub.view.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.isis3510.growhub.offline.NetworkUtils
import com.isis3510.growhub.viewmodel.AuthViewModel
import com.isis3510.growhub.cache.RegistrationCache

@Composable
fun InterestsScreen(
    viewModel: AuthViewModel,
    onContinueSuccess: () -> Unit,
    onGoBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val skillsList = uiState.availableSkills
    val selectedSkillIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        if (skillsList.isEmpty()) {
            viewModel.fetchSkillsList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select your interests",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(skillsList) { skill ->
                SkillItem(
                    skillName = skill.name,
                    skillId = skill.id,
                    isSelected = selectedSkillIds.contains(skill.id),
                    onCheckedChange = { checked ->
                        if (checked) selectedSkillIds.add(skill.id)
                        else selectedSkillIds.remove(skill.id)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val hasNetwork = NetworkUtils.isNetworkAvailable(context)
                if (!hasNetwork) {
                    Toast.makeText(
                        context,
                        "Please check your internet connection.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // 🗃️ Guardamos intereses seleccionados en la LRU
                    RegistrationCache.put("selectedSkills", selectedSkillIds.toList())

                    viewModel.finalizeUserRegistration(
                        selectedSkills = selectedSkillIds.toList(),
                        onSuccess = {
                            Toast.makeText(context, "Registered successfully", Toast.LENGTH_LONG).show()
                            onContinueSuccess()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5669FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "CONTINUE", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { onGoBack() }) {
            Text(text = "Go back")
        }
    }
}

@Composable
fun SkillItem(
    skillName: String,
    skillId: String,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = skillName, color = Color.Black)
            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
