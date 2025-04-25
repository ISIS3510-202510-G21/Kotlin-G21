package com.isis3510.growhub.model.objects

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rememberMe: Boolean = true,
    val userRole: String = "",

    // Nuevo: Almacena el userId que FirebaseAuth devuelva:
    val userId: String? = null,

    // Lista de skills disponibles, viene de la colección "skills"
    val availableSkills: List<Skill> = emptyList()
)

// Se define un data class para los skills
data class Skill(
    val id: String = "",
    val name: String = ""
)
