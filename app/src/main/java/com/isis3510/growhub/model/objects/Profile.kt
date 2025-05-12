package com.isis3510.growhub.model.objects

import com.isis3510.growhub.local.data.ProfileEntity

data class Profile (
    val name: String,
    val following: Int,
    val followers: Int,
    val description: String ,
    val interests: List<String>,
    val profilePicture: String
)

fun Profile.toEntity(): ProfileEntity {
    return ProfileEntity(
        name = name,
        following = following,
        followers = followers,
        description = description,
        interests = interests,
        profilePicture = profilePicture
    )
}

fun ProfileEntity.toModel(): Profile {
    return Profile(
        name = name,
        following = following,
        followers = followers,
        description = description,
        interests = interests,
        profilePicture = profilePicture
    )
}