package com.isis3510.growhub.model.objects

data class Profile (
    val name: String = "",
    val following: Int = 0,
    val followers: Int = 0,
    val aboutMe: String = "",
    val interests: List<String> = listOf(),
    val profilePictureUrl: String = ""
)