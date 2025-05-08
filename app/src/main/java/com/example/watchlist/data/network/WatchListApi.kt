package com.example.watchlist.data.network

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface WatchListApi {

    //check out proguard to hide, its in build.gradle.kts
    @Headers("Cache-Control: no-cache")
    @GET("releases/?apiKey=GIbEnlmBeVyx99O1YxAUOihxpEGrGt55mPBH8a6H")
    suspend fun getUpcomingReleases(): NetworkMovieContainer

    @Headers("Cache-Control: no-cache")
    @GET("sources/?apiKey=GIbEnlmBeVyx99O1YxAUOihxpEGrGt55mPBH8a6H")
    suspend fun getServiceRegions(): List<NetworkService>

    @Headers("Cache-Control: no-cache")
    @GET("title/{titleId}/details/?apiKey=GIbEnlmBeVyx99O1YxAUOihxpEGrGt55mPBH8a6H")
    suspend fun getTitleDetail(@Path("titleId") titleId: Int): TitleDetail
}

//https://api.watchmode.com/v1/title/3264918/details/?apiKey=GIbEnlmBeVyx99O1YxAUOihxpEGrGt55mPBH8a6H




