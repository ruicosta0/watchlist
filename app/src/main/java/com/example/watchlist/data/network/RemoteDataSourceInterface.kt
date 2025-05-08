package com.example.watchlist.data.network

import com.example.watchlist.data.domain.TitleDetailDomain
import com.example.watchlist.data.local.movies.DatabaseMovie
import com.example.watchlist.data.local.streamingService.RegionEntity
import com.example.watchlist.data.local.streamingService.ServiceEntity

interface RemoteDataSourceInterface {

    suspend fun refreshMovies() : Array<DatabaseMovie>
    suspend fun refreshServiceSources() : Pair<List<ServiceEntity>, List<RegionEntity>>
    suspend fun getTitleDetails(titleId: Int) : TitleDetailDomain

}