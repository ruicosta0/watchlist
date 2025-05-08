package com.example.watchlist.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.watchlist.data.domain.Movie
import com.example.watchlist.data.domain.StreamingService
import com.example.watchlist.data.domain.TitleDetailDomain
import com.example.watchlist.data.local.movies.DatabaseMovie
import com.example.watchlist.data.local.movies.MovieDao
import com.example.watchlist.data.local.streamingService.RegionEntity
import com.example.watchlist.data.local.streamingService.ServiceDao
import com.example.watchlist.data.local.streamingService.ServiceEntity
import com.example.watchlist.data.local.streamingService.ServiceWithRegions
import com.example.watchlist.data.network.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import java.util.Observer

class MovieRepository(val remoteDataSource: RemoteDataSource, val dataBase: Pair<MovieDao, ServiceDao>) {

    private val movieDao = dataBase.first
    private val serviceDao = dataBase.second

    companion object {
        val TAG = "WatchListApp"
    }

    init {
        Log.i(TAG,"MovieRepository created")
    }

    //check internet availability
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) // Ensures actual access
    }

    //return titles from network and saved to database
    suspend fun repoRefreshMovies() { // need to run sequentially, use coroutine scope for asynchronous parallel
        try {
            val releases = remoteDataSource.refreshMovies()
            Log.d(TAG,"movieRepo what does the releases type ${releases::class.simpleName} and size ${releases.size}")
            saveMovieReleasesToDatabase(releases)
        } catch(e: Exception) {Log.d(TAG," Error refreshing movies $e.message",e)}
    }
    //return streaming services and respective regions from network and save to database
    suspend fun repoRefreshServiceSources() {
        try {
            val serviceSource = remoteDataSource.refreshServiceSources()
            withContext(Dispatchers.IO) {
                //insert services to database
                Log.d(TAG, "inserting services ${serviceSource.first.size}")
                serviceDao.insertService(serviceSource.first)
                Log.d(TAG, "Services inserted successfully")

                // insert regions to database
                Log.d(TAG, "inserting regions ${serviceSource.second.size}")
                serviceDao.insertRegions(serviceSource.second)
                Log.d(TAG, "Regions inserted successfully")
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error refreshing servicesources ${e.message}", e)
        }
    }
    //return title details from network
    suspend fun getTitle(titleId: Int) : TitleDetailDomain {
            try {
                val titleDetail = remoteDataSource.getTitleDetails(titleId)
                Log.d(TAG, "MovieRepo title Detail ${titleDetail.title}")
                return titleDetail
            } catch (e: Exception) {
                Log.d(TAG, "Error returning title details ${e.message}", e)
                throw e
            }
        }

    //live data for UI layer. domain objects returned from database
    val moviesToday: LiveData<List<Movie>> = movieDao.getMovieByDate(LocalDate.now().toString()).map { databaseMovies ->  databaseMovies.asDomainModel() } //you cannot pass in a liveData (wrapper), thats way you need the .map after getMovieByDate and the {databaseMovies ->....)
    val moviesAll: LiveData<List<Movie>> = movieDao.getMovies().map { databaseMovies -> databaseMovies.asDomainModel()}
    val moviesTodayPlusSeven: LiveData<List<Movie>> = movieDao.getNextSevenDaysMovies(LocalDate.now().toString(),LocalDate.now().plusDays(7).toString() ).map {databaseMovies -> databaseMovies.asDomainModel()  }
    val moviesTodayOnwards: LiveData<List<Movie>> = movieDao.getTodayOnwards(LocalDate.now().toString()).map { databaseMovies -> databaseMovies.asDomainModel()  }
    val watchlistTitles: LiveData<List<Movie>> = movieDao.getAllWatchlistTitles().map { databaseMovies -> databaseMovies.asDomainModel()  }

    //retrieve list for streaming service checkboxes
  // val streamingServices: LiveData<List<StreamingService>> = serviceDao.getServicesByRegion("").map { streamingService -> streamingService.asDomainModelServiceWithRegion() }

    //streaming services available in the region of the user (countryCode)
    fun getServiceByRegion(countryCode:String?) : LiveData<List<StreamingService>> {
        return serviceDao.getServicesByRegion(countryCode).map { streamingService -> streamingService.asDomainModelServiceWithRegion() }
    }

    //insert movies into database
    suspend fun saveMovieReleasesToDatabase(releases: Array<DatabaseMovie>) { // no return needed as action is being performed and not returning a data set for example
        withContext(Dispatchers.IO) {
            val extantMovies = movieDao.getAllMovies() ?: emptyList()//get all the movies in the database
            Log.d("Database", "Database ${extantMovies.size}")

            // merge data from API call with existing database data
            val mergedMovies = releases.map { release ->

                // find a matching movie in the database
                val extantMovie = extantMovies.firstOrNull {it.id == release.id}
                Log.d(TAG,"Extant movies ${extantMovie?.title}")

                //if movie exists in database already retain watchlist property of the movie
                release.copy(watchlist = extantMovie?.watchlist ?: 0)
            }
            movieDao.insertAll(*mergedMovies.toTypedArray())
        }
    }

    //streaming service logo from service database
    suspend fun getStreamingServiceLogoRepo(serviceId: Int?) : String? {
        return withContext(Dispatchers.IO) {
            try {
                val logo = serviceDao.getStreamingServiceLogo(serviceId) // pass in Int id to retrieve logo String
                return@withContext logo
            } catch (e:Exception) {
            Log.d(TAG, "Error retrieving streaming service logo", e)
                throw e
            }
        }
    }

    //update title in database watchlist property to 1 (true)
    suspend fun addToWatchList(titleId: Int) {
        withContext(Dispatchers.IO) {
            try{
                movieDao.addToWatchlist(titleId)
            } catch (e: Exception) {
                Log.d(TAG, "Error adding to watch list", e)
            }
        }
    }

    //update title in database watchlist property to 1 (false)
    suspend fun removeFromWatchList(titleId: Int) {
        withContext(Dispatchers.IO) {
            try{
                movieDao.removeFromWatchlist(titleId)
                Log.d(TAG, "movie removed $titleId")
            } catch (e: Exception) {
                Log.d(TAG, "Error removing from watch list", e)
            }
        }
    }

    suspend fun getMovieById(titleId: Int): DatabaseMovie {
        return withContext(Dispatchers.IO) {
            try{
                val title = movieDao.getMovieById(titleId)
                return@withContext title
            } catch (e: Exception) {
                Log.d(TAG, "Error retrieving movie by Id", e)
                throw e
            }
        }
    }
    //refresh movies live data, data is shared across fragments
    suspend fun moviesRefresh() {
        return withContext(Dispatchers.IO) {
            try{
                movieDao.getAllMovies()
            } catch (e: Exception) {
                Log.d(TAG, "Error refreshing all movies", e)
                throw e
            }
        }
    }

    suspend fun clearDatabase() {
        movieDao.clear()
    }
}

//conversion of database objects to domain model for UI display
fun List<DatabaseMovie>.asDomainModel() : List<Movie> { //passing in a list so 'it.' iterator is in the Movie()
    return this.map {
        Movie (
            title = it.title ?: "N/A",
            id = it.id,
            sourceId = it.sourceId,
            service = it.service ?: "N/A",
            type = it.type ?: "N/A",
            date = it.date ?: "N/A",
            watchlist = it.watchlist?: 0
        )
    }
}

fun List<ServiceWithRegions>.asDomainModelServiceWithRegion() : List<StreamingService> {
    return this.map {
        StreamingService(
            id = it.service.watchId,
            name = it.service.name,
            type = it.service.type,
            logo = it.service.logoUrl
        )
    }
}
