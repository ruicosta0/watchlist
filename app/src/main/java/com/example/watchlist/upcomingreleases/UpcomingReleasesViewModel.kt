package com.example.watchlist.upcomingreleases

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.watchlist.base.BaseViewModel
import com.example.watchlist.data.domain.Movie
import com.example.watchlist.data.repository.MovieRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.map
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.watchlist.R
import com.example.watchlist.base.NavigationCommand
import com.example.watchlist.data.domain.StreamingService
import com.example.watchlist.data.local.streamingService.ServiceWithRegions
import com.example.watchlist.upcomingreleases.UpcomingReleasesFragmentDirections
import com.example.watchlist.watchlist.MovieAction
import com.example.watchlist.watchlist.WatchlistFragmentDirections
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class UpcomingReleasesViewModel(app: Application, val datasource: MovieRepository):BaseViewModel(app) {

    companion object {
        val TAG = "WatchListApp"
    }

    var filter: MutableLiveData<Int> =
        MutableLiveData() //used to filter movieList by switching movieRepo/datasource functions

    //val selectedServices: MutableLiveData<MutableSet<String?>> = MutableLiveData() //to be set via clicking the checkboxes
    val selectedServices: MutableLiveData<Set<Int?>> = MutableLiveData(mutableSetOf())

    // LiveData to store checkbox states
    private val _checkboxStates = MutableLiveData<Map<Int, Boolean>>()
    val checkboxStates: LiveData<Map<Int, Boolean>> get() = _checkboxStates

    // Function to update a checkbox state
    fun updateCheckboxState(id: Int, isChecked: Boolean) {
        val currentStates = _checkboxStates.value ?: emptyMap()
        _checkboxStates.value = currentStates + (id to isChecked) // Update the map
        Log.d(TAG, "VIEWMODEL $currentStates and ${_checkboxStates.value}")
    }

    // Function to get the current state of a specific checkbox
    fun getCheckboxState(id: Int): Boolean {
        return _checkboxStates.value?.get(id) ?: false // Default to unchecked
    }
    //data for recycler view in upcoming release fragment, sourced from movie list(by date options), selected streaming services and services available in user country
    val filteredMovieList: MediatorLiveData<List<Movie>> by lazy { // use by lazy to defers initialization until filteredMovieList is accessed
        MediatorLiveData<List<Movie>>().apply {
            // Add sources, filteredMovieList interdependent on 3 different filters. Logic applied each time addSource changes
            addSource(moviesList) { combineFilters() }
            addSource(selectedServices) { combineFilters() }
            addSource(filterServiceOptions) { combineFilters() }
        }
    }

    // Combine filtering logic for all sources, called each time one of the addSource() changes
    private fun combineFilters() {
        val movies = moviesList.value.orEmpty() // Movies from moviesList (date options)
        val services = selectedServices.value.orEmpty() // Selected services
        val serviceOptions = filterServiceOptions.value.orEmpty() // Available services in user country
        val serviceIds = serviceOptions.map { it.id }.toSet() // Convert to Set for faster lookup

        // Apply combined filters
        filteredMovieList.value = movies.filter { movie ->
            val matchesServiceOptions =
                serviceIds.contains(movie.sourceId) // Filter by serviceOptions, sourceID is streaming service eg Netlix, Hulu etc
            val matchesSelectedServices =
                services.isNullOrEmpty() || services.contains(movie.sourceId) // Filter by selectedServices
            matchesServiceOptions && matchesSelectedServices // Movie must satisfy both conditions
        }
    }


    // set up for filtering by country code
    private val _countryCode = MutableLiveData<String?>()
    val countryCode: LiveData<String?> get() = _countryCode

    //availabel services in user country
    val filterServiceOptions: LiveData<List<StreamingService>> =
        countryCode.switchMap { code -> //switchMap allows reactive live monitoring of countryCode live data
            datasource.getServiceByRegion(code)
        }


    // call MovieRepository functions to select live data, user chooses from options menu in UI
    val moviesToday = datasource.moviesToday
    val moviesTodayPlusSeven = datasource.moviesTodayPlusSeven
    val moviesTodayOnwards = datasource.moviesTodayOnwards
    val moviesAll = datasource.moviesAll
    val watchlistTitles = datasource.watchlistTitles // LiveData<List<Movie>>

    //used for watchlist in Watchlist fragment
    val allTitles: LiveData<List<Movie>> get() =  watchlistTitles

    init {
        Log.d(TAG, "init ${selectedServices}")
        loadMovies()
    }

    fun loadMovies() {
        Log.d(TAG, "loadMovies viewModel called")
        viewModelScope.launch {
            try {
                Log.d(TAG, "viewModel before calling repoRefreshMovies")
                datasource.repoRefreshMovies()
                datasource.repoRefreshServiceSources()
                Log.d(TAG, "viewModel after calling repoRefreshMovies")
                //setFilter(1)
                Log.d(TAG, "viewModel set filter called successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during loadMovies execution $e.message}", e)
            }
        }
    }

    val moviesList: LiveData<List<Movie>> =
        //fragment is observing movies list which calls MovieRepository functions
        filter.switchMap { //switch map is live monitoring and reacting to filter LiveData
            Log.d(TAG, "switchMap called ${filter.value}")
            when (it) {
                1 -> {
                    moviesToday
                }

                2 -> {
                    moviesTodayPlusSeven
                }

                3 -> {
                    moviesTodayOnwards
                }

                4 -> {
                    moviesAll
                }
                else -> {
                    moviesTodayPlusSeven
                }
            }
        }
    //navigate to watchlist frag
    fun navigateToWatchList() {
        navigationCommand.postValue(NavigationCommand.To(UpcomingReleasesFragmentDirections.actionUpcomingReleasesFragmentToWatchList(),null))
    }
    //navigate to upcoming releases
    fun navigateToUpcomingReleases() {
        navigationCommand.postValue((NavigationCommand.To(WatchlistFragmentDirections.actionWatchListToUpcomingReleasesFragment(),null)))
    }
    //sets filter from upcoming releases UI options
    fun setFilter(option: Int) {
        filter.value = option
    }

    //check connectivity & trigger fetchTitleDetails
    fun onMovieClicked(context:Context, movie: Movie, sourceFragment: Int) {
        if(datasource.isInternetAvailable(context)) { //check connectivity first
            fetchTitleDetails(movie, sourceFragment)
        } else {
            triggerNoInternetMessage() //handle no internet
        }
    }

    //use title id to fetch title details from network and pass to TitleDetailsFragment via bundle
    fun fetchTitleDetails(movie: Movie, sourceFragment: Int) {
        viewModelScope.launch {
            Log.d(TAG, "onMovieClicked ${movie.id} and watchlist ${movie.watchlist}")
            val titleDetail = datasource.getTitle(movie.id)
            val bundle = Bundle().apply{
                putParcelable("titleDetail", titleDetail)
                putInt("source", movie.sourceId)
                putInt("savedToWatchlist", movie.watchlist) //is movie saved as watchList T/F
            }
            when (sourceFragment) {
                0 -> navigationCommand.postValue( // navigate from upcoming releases
                    NavigationCommand.To(UpcomingReleasesFragmentDirections.actionUpcomingReleasesFragmentToMovieDetailFragment(), bundle)
                )
                1 -> navigationCommand.postValue( //navigate from watchlist
                    NavigationCommand.To(WatchlistFragmentDirections.actionWatchListToTitleDetailFragment(),bundle))
            }
        }
    }

    // handle user selections from dialog box in watchlist fragment - remove from watchlist, navigate to details or cancel action
    fun handleMovieOptions(context: Context, movie: Movie, action: MovieAction, sourceFragment: Int) {
        when (action) {
            MovieAction.REMOVE_FROM_WATCHLIST -> {
                viewModelScope.launch {
                    Log.d(TAG, "onMovieClicked Watchlistfrag ${movie.id} and watchlist ${movie.watchlist}")
                    datasource.removeFromWatchList(movie.id)
                    Log.d(TAG, "onMovieClicked Watchlistfrag ${movie.id} and watchlist ${movie.watchlist}")

                }
            }
            MovieAction.NAVIGATE_TO_DETAILS -> {
                onMovieClicked(context, movie, sourceFragment)
            }
            MovieAction.RETURN -> {
                // Do nothing, just dismiss the dialog
            }
        }
    }

    //user selects streaming services from horizontal ribbon
    fun updateSelectedServices(selectedFilter: Int?) {
        Log.d(TAG, "UPDATE FILTERS before ${selectedFilter} and ${selectedServices.value}")
        val currentSet = selectedServices.value.orEmpty().toMutableSet()

        if (currentSet.contains(selectedFilter))
            currentSet.remove(selectedFilter)
        else {
            currentSet.add(selectedFilter)
        }
        selectedServices.value = currentSet

        Log.d(TAG, "after add ${selectedServices.value}")
    }
    //update country code from user country location
    fun updateCountryCode(userCountryCode: String?) {
        Log.d(TAG, "UPDATE COUNTRY CODE CALLED ${userCountryCode}")
        _countryCode.value = userCountryCode
    }
}