package com.isis3510.growhub.utils

import com.google.firebase.Timestamp

data class SearchEventClick(
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String,
    val clickType: String
)
