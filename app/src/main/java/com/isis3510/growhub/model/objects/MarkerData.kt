package com.isis3510.growhub.model.objects

import com.google.android.gms.maps.model.LatLng

data class MarkerData(
    val id: String,
    val position: LatLng,
    val title: String,
    val snippet: String? = null
)