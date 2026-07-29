package com.ckck.android.api

import retrofit2.http.GET
import retrofit2.http.Query

interface TransitousService {
    @GET("api/v6/stoptimes")
    suspend fun getStopTimes(
        @Query("stopId") stopId: String? = null,
        @Query("center") center: String? = null,
        @Query("time") time: String? = null,
        @Query("arriveBy") arriveBy: Boolean = false,
        @Query("direction") direction: String? = null,
        @Query("window") window: Int? = null,
        @Query("mode") mode: List<String>? = null,
        @Query("n") n: Int? = null,
        @Query("radius") radius: Int? = null,
        @Query("exactRadius") exactRadius: Boolean = false,
        @Query("fetchStops") fetchStops: Boolean = false,
        @Query("pageCursor") pageCursor: String? = null,
        @Query("withScheduledSkippedStops") withScheduledSkippedStops: Boolean = false,
        @Query("language") language: List<String>? = null,
        @Query("withAlerts") withAlerts: Boolean = true
    ): StopTimesResponse
}
