package com.isis3510.growhub.view.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.isis3510.growhub.R
import com.isis3510.growhub.cache.RegistrationCache
import com.isis3510.growhub.offline.NetworkUtils
import com.isis3510.growhub.viewmodel.AuthViewModel
import androidx.compose.animation.animateColorAsState


@Composable
fun InterestsScreen(
    viewModel: AuthViewModel,
    onContinueSuccess: () -> Unit,
    onGoBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val skillsList = uiState.availableSkills
    /** IDs actualmente seleccionados **/
    val selectedSkillIds = remember { mutableStateListOf<String>() }

    /** Traemos skills de Firestore si aún no estaban **/
    LaunchedEffect(Unit) {
        if (skillsList.isEmpty()) viewModel.fetchSkillsList()
    }

    /** Colores **/
    val primaryBlue = Color(0xFF5669FF)
    val cardBorder = Color(0xFF161616)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        /* -------- Título -------- */
        Text(
            text = "Select your interests",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),

        )

        Spacer(modifier = Modifier.height(16.dp))

        /* -------- Grilla de intereses -------- */
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(skillsList, key = { it.id }) { skill ->
                val isSelected = selectedSkillIds.contains(skill.id)
                SkillCard(
                    skillName = skill.name,
                    isSelected = isSelected,
                    onToggle = {
                        if (isSelected) selectedSkillIds.remove(skill.id)
                        else selectedSkillIds.add(skill.id)
                    },
                    primaryBlue = primaryBlue,
                    borderColor = cardBorder
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        /* -------- Botón CONTINUE -------- */
        Button(
            onClick = {
                val hasNetwork = NetworkUtils.isNetworkAvailable(context)
                if (!hasNetwork) {
                    Toast.makeText(context, "Please check your internet connection.", Toast.LENGTH_LONG).show()
                } else {
                    RegistrationCache.put("selectedSkills", selectedSkillIds.toList())
                    viewModel.finalizeUserRegistration(
                        selectedSkills = selectedSkillIds.toList(),
                        onSuccess = {
                            Toast.makeText(context, "Registered successfully", Toast.LENGTH_LONG).show()
                            onContinueSuccess()
                        },
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
        ) {
            Text(text = "CONTINUE", color = Color.White)
        }

        Spacer(modifier = Modifier.height(6.dp))

        /* -------- Go back -------- */
        TextButton(onClick = onGoBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Go back")
        }
    }
}

/* ------------------------------------------------------------------------- */
/* -----------------------------  COMPONENTES ------------------------------ */
/* ------------------------------------------------------------------------- */

/**
 * Tarjeta individual de Skill.
 *
 * @param skillName   nombre visible
 * @param isSelected  determina si se muestra overlay con check
 * @param onToggle    callback al tocar la tarjeta
 */
@Composable
private fun SkillCard(
    skillName: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    primaryBlue: Color,
    borderColor: Color
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF3F3F3) else Color.White,
        label = "skillCardBg"
    )

    Card(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = animatedBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            /* Icono (por defecto uno genérico) */
            Icon(
                painter = painterResource(id = R.drawable.ic_interest_default),
                contentDescription = null,
                tint = primaryBlue,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = skillName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = Color.Black
            )
        }

        /* Overlay con “check” cuando está seleccionado */
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(primaryBlue.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryBlue,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }
        }
    }
}