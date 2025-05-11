package com.isis3510.growhub.local.data

import com.isis3510.growhub.model.objects.Event

object GlobalData {
    var upcomingEvents: List<Event> = emptyList()
    var nearbyEvents: List<Event> = emptyList()
    var myEventsUpcoming: List<Event> = emptyList()
    var myEventsPrevious: List<Event> = emptyList()
    var myEventsCreatedByMe: List<Event> = emptyList()
}