package com.ckck.android.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data class StationDetail(val stationId: String, val stationName: String) : Route

    @Serializable
    data class TripDetail(val from: String, val to: String) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Favorites : Route
}
