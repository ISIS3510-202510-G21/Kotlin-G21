package com.isis3510.growhub.model.objects

data class Profile (
    val name: String,
    val following: Int,
    val followers: Int,
    val aboutMe: String ,
    val interests: List<String>,
    val profilePictureUrl: String
)