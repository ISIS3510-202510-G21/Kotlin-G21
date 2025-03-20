package com.isis3510.growhub.model.objects

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val userRole: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rememberMe: Boolean = true,
    val userRole: String = ""
)
