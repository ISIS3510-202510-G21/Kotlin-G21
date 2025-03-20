package com.isis3510.growhub.model.objects

data class AppUser(
    val email: String = "",
    val name: String = "",
    val user_type: String = "host", // Valor por defecto
    val username: String = ""      // Si en un futuro decides usarlo
)
