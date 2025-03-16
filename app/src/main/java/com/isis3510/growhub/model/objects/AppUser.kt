package com.isis3510.growhub.model.objects

data class AppUser(
    val email: String = "",
    val name: String = "",
    val userType: String = "", // Valor por defecto
    val username: String = ""      // Si en un futuro decides usarlo
)
