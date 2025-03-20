package com.isis3510.growhub.model.objects

data class AppUser(
    val email: String = "",
    val name: String = "",
    val userType: String = "host", // Valor por defecto
)
