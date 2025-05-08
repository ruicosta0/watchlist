package com.example.watchlist.data.network

import android.util.Log
import com.example.watchlist.data.domain.TitleDetailDomain
import com.example.watchlist.data.local.movies.DatabaseMovie
import com.example.watchlist.data.local.streamingService.RegionEntity
import com.example.watchlist.data.local.streamingService.ServiceEntity
import com.example.watchlist.data.repository.MovieRepository.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class RemoteDataSource(): RemoteDataSourceInterface {
    //run Dispatchers.IO to avoid main thread
    //retrieve title releases from watchmode api and transform to DatabaseMovie returned in array
    override suspend fun refreshMovies() : Array<DatabaseMovie> {
        return withContext(Dispatchers.IO) { //remember the return before withContext as we want an Array<DatabaseMovie> back
          try {
                val networkContainer = RetrofitInstance.movies.getUpcomingReleases()

                val arrayDatabaseMovie = networkContainer.asDatabaseModel()
                Log.d(TAG, "RemoteDataSource API call returns  ${arrayDatabaseMovie::class.simpleName} and ${arrayDatabaseMovie.size}")
              arrayDatabaseMovie.forEach { movie ->
                  Log.d("DatabaseMovies", "Movie: $movie")
              }
              return@withContext arrayDatabaseMovie //@withContext required if called from the withContext(Dispatcher.IO)
            } catch (e:Exception) {
             Log.d(TAG, "Error refreshMovies: ${e.message}")
              throw e
            }
        }
    }

    override suspend fun refreshServiceSources() : Pair<List<ServiceEntity>,List<RegionEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val netWorkServiceContainer = RetrofitInstance.movies.getServiceRegions()
                Log.d(TAG,"netWorkServiceContainer ${netWorkServiceContainer.toString()}")
                val listNetworkServices = netWorkServiceContainer.asDatabaseModel()
                Log.d(TAG,listNetworkServices.toString())
                Log.d(TAG, "Number of services: ${listNetworkServices.first.size}, Regions: ${listNetworkServices.second.size}")
                listNetworkServices.first.forEach { Log.d(TAG, "WatchId: ${it.watchId}, Service: ${it.name}") }
                listNetworkServices.second.forEach { Log.d(TAG, "serviceOwnerId: ${it.serviceOwnerId},RegionId: ${it.regionId}, ID: ${it.regionCode}") }
                return@withContext listNetworkServices
            } catch (e:Exception) {
                Log.d(TAG, "Unexpected Error refreshServiceSources: ${e.message}")
                throw e
            } catch (e: IOException) {
                Log.d(TAG, "Network error refreshServicesSources: ${e.message}")
                throw e
            }
        }
    }

    override suspend fun getTitleDetails(titleId: Int) : TitleDetailDomain {
        return withContext(Dispatchers.IO) {
            try {
                val titleDetail = RetrofitInstance.movies.getTitleDetail(titleId)
                Log.d(TAG, "titleDetail $titleDetail")
                val title = titleDetail.asDomainModelTitleDetail()
                Log.d(TAG, "title as domain model $title")
                return@withContext title
            } catch (e:Exception) {
                Log.d(TAG, "Unexpected Error titleDetail: ${e.message}")
                throw e
            } catch (e: IOException) {
                Log.d(TAG, "Network error titleDetail: ${e.message}")
                throw e
            }
        }
    }

}