package com.ckck.android.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StopTimesResponse(
    @SerialName("stopTimes") val stopTimes: List<StopTime>,
    @SerialName("place") val place: Place,
    @SerialName("previousPageCursor") val previousPageCursor: String? = null,
    @SerialName("nextPageCursor") val nextPageCursor: String? = null
)

@Serializable
data class StopTime(
    @SerialName("place") val place: Place,
    @SerialName("mode") val mode: String? = null,
    @SerialName("realTime") val realTime: Boolean = false,
    @SerialName("headsign") val headsign: String? = null,
    @SerialName("tripId") val tripId: String? = null,
    @SerialName("routeShortName") val routeShortName: String? = null,
    @SerialName("routeLongName") val routeLongName: String? = null,
    @SerialName("agencyName") val agencyName: String? = null,
    @SerialName("cancelled") val cancelled: Boolean = false,
    @SerialName("tripCancelled") val tripCancelled: Boolean = false,
    @SerialName("displayName") val displayName: String = "",
    @SerialName("routeColor") val color: String?,
    @SerialName("routeTextColor") val textColor: String?
)

@Serializable
data class Place(
    @SerialName("name") val name: String,
    @SerialName("stopId") val stopId: String? = null,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lon") val lon: Double? = null,
    @SerialName("arrival") val arrival: String? = null,
    @SerialName("departure") val departure: String? = null,
    @SerialName("scheduledArrival") val scheduledArrival: String? = null,
    @SerialName("scheduledDeparture") val scheduledDeparture: String? = null,
    @SerialName("track") val track: String? = null,
    @SerialName("scheduledTrack") val scheduledTrack: String? = null,
    @SerialName("tz") val tz: String? = null,
    @SerialName("cancelled") val cancelled: Boolean = false
)
