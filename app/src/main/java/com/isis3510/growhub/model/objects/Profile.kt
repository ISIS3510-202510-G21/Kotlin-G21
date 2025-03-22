package com.isis3510.growhub.model.objects

data class Profile (
    val name: String,
    val following: Int,
    val followers: Int,
    val description: String ,
    val interests: List<String>,
    val profilePicture: String
)