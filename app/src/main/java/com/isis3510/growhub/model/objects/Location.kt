package com.isis3510.growhub.model.objects

import com.google.android.gms.maps.model.LatLng

data class Location(
    val id: String,
    val address : String = "",
    val city: String = "",
    val details: String = "",
    val latitude: Double,
    val longitude: Double,
    val university: Boolean = true,
)
{
    fun getInfo(): String {
        return "$address, $city"
    }

    fun getCoordinates(): LatLng {
        return LatLng(latitude, longitude)
    }
}