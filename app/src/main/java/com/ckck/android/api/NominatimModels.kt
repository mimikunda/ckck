package com.ckck.android.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimPlace(
    @SerialName("place_id") val placeId: Long? = null,
    @SerialName("display_name") val displayName: String,
    @SerialName("lat") val lat: String,
    @SerialName("lon") val lon: String,
    @SerialName("name") val name: String? = null,
    @SerialName("type") val type: String? = null
)
