package com.example.watchlist.upcomingreleases

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.watchlist.R
import com.example.watchlist.base.BaseFragment
import com.example.watchlist.base.MovieListener
import com.example.watchlist.databinding.FragmentUpcomingReleasesBinding
import com.example.watchlist.upcomingreleases.UpcomingReleasesViewModel.Companion
import com.example.watchlist.utils.setDisplayHomeAsUpEnabled
import com.example.watchlist.utils.setup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.watchlist.BuildConfig
import com.example.watchlist.base.NavigationCommand
import com.example.watchlist.data.domain.Movie
import com.example.watchlist.data.domain.StreamingService
import com.example.watchlist.launch.LaunchFragmentDirections
import com.example.watchlist.watchlist.WatchlistFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.IOException
import java.util.Locale

class UpcomingReleasesFragment: BaseFragment() {

    private lateinit var binding: FragmentUpcomingReleasesBinding
    override val _viewModel: UpcomingReleasesViewModel by activityViewModel() //activity scoped to prevent losing view model after navigating

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var countryCode: String

    private var items: List<StreamingService> = listOf() // Initialize as an empty list

    companion object {
        val TAG = "WatchListApp"
    }

    private var geocoderJob: Job? = null //store coroutine job

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        Log.d(TAG, "onCreateView() called")

        val layoutId = R.layout.fragment_upcoming_releases
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel

        // Construct a FusedLocationProviderClient, the Google Play Services Location API
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        //check if user has foreground permissions enabled to establish country location
        checkPermissions()


        val ribbonAdapter = RibbonAdapter(emptyList(), _viewModel) { selectedFilter -> //lambda is third argument in Ribbon Adapter in parameter
           Log.d(TAG, "Ribbon Adapter Clicked ${selectedFilter}")
            _viewModel.updateSelectedServices(selectedFilter)
        }

        //retain checked streaming service states
        _viewModel.checkboxStates.observe(viewLifecycleOwner) { states ->
            val updatedItems = items.map { service ->
                service.isChecked = states[service.id] ?: false
                service
            }
            ribbonAdapter.updateItems(updatedItems)
        }
        //observe streaming service options in users country
        _viewModel.filterServiceOptions.observe(viewLifecycleOwner) { services ->
            Log.d("DiffCallback", "Observed services: $services")
            items = services
            ribbonAdapter.updateItems(services)
        }
        // horizontal ribbon recycler for streaming services
        val ribbonRecyclerView =  binding.ribbonRecyclerView
        ribbonRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ribbonAdapter
        }
        //setup recycler view for title list
        setupRecyclerView()

        setHasOptionsMenu(true) //required for menu options

        //refresh title list api call available through screen swipe refresh
        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadMovies()
            binding.refreshLayout.isRefreshing = false
        }

        //hide service ribbon when movie list is being scrolled
//        binding.moviesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                if ( dy != 0) {
//                    ribbonRecyclerView.visibility = View.GONE
//                }
//            }
//            //show movie list when screen is idle
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    ribbonRecyclerView.visibility = View.VISIBLE
//                }
//            }
//        })

//        _viewModel.showNoInternetMessage.observe(viewLifecycleOwner) { show ->
//            if (show) {
//                Toast.makeText(context, "No internet connection. Please enable internet.", Toast.LENGTH_SHORT).show()
//            }
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onStart() {
        super.onStart()
    }

    //filter to today plus seven titles
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewModel.setFilter(2)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        findUserCountryLocation()
    }

    private fun setupRecyclerView() {
        // Pass in click listener to adapter param
        val movieListener = MovieListener { movie ->
            _viewModel.onMovieClicked(requireContext(), movie, NAVIGATE_FROM_UPCOMING_RELEASES) } //NAVIGATE_FROM_UPCO...tells viewmodel click came from upcoming releases fragment
        val adapter = MoviesListAdapter(movieListener::onClick)

        // Set up the adapter
        binding.moviesRecyclerView.setup(adapter)

        Log.d("RecyclerView", "RecyclerView setup complete. Adapter attached=${binding.moviesRecyclerView.adapter != null}")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    //navigation options from upcoming fragments
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.navigate_to_watchlist_from_releases) {
            _viewModel.navigateToWatchList() //go to watchlist
            return true //needed as fun returns Boolean
        }
            if (item.itemId != android.R.id.home) { //android.R.id.home is navigate up arrow
                _viewModel.setFilter(
                    when (item?.itemId) {
                        R.id.show_todays_releases -> 1
                        R.id.show_next_seven_releases -> 2
                        R.id.show_today_onwards -> 3
                        R.id.show_all_movie_releases -> 4
                        else -> 1
                    }
                )
                return true
            }
            return false //allows handling onSupportNavigateUp()(R.id.home) to navigate up
        }


// user only needs my-location layer permissions, ie foreground location permission and enabling of device settings
    private fun checkPermissions() {
        if (foregroundLocationPermissionApproved()) {
            findUserCountryLocation()
        } else {
            requestForegroundLocationPermissions()
        }
    }

    // check foreground permission
    @TargetApi(29)
    fun foregroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION))
        Log.i(TAG,"foregroundLocationApproved is ${foregroundLocationApproved}")
        return foregroundLocationApproved
    }

    private fun requestForegroundLocationPermissions() {
        if (foregroundLocationPermissionApproved())
            return
        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        val resultCode = REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE

        Log.d(TAG, "Request foreground only location permission")
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    // handle result of requestPermissions, ask user to enable via dialog or if already enabled proceed to check device location enablement
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)
        {
            // Permission denied.
            Snackbar.make(binding.root,
                R.string.location_notifcation, Snackbar.LENGTH_LONG
            )
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
                countryCode = ""
                _viewModel.updateCountryCode(countryCode)
                Log.d(TAG, "ISO Country Code $countryCode")
                Toast.makeText(context,R.string.location_connectivity, Toast.LENGTH_SHORT).show()
        } else {
            findUserCountryLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun findUserCountryLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            Log.d(TAG, "Location Enabled: ${isLocationEnabled(requireContext())}")

            if (!isLocationEnabled(requireContext())) {
                _viewModel.updateCountryCode("")
                Toast.makeText(context, R.string.location_connectivity, Toast.LENGTH_SHORT).show()
            } else {
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude

                    CoroutineScope(Dispatchers.IO).launch { //manual scope
                        try {
                            val geocoder = Geocoder(requireContext(), Locale.getDefault())
                            val address = geocoder.getFromLocation(latitude, longitude, 1)

                            withContext(Dispatchers.Main) {
                                if (!address.isNullOrEmpty()) {
                                    val countryCode = address[0].countryCode ?: "unknown"
                                    Log.d(TAG, "ISO Country Code: $countryCode")
                                    _viewModel.updateCountryCode(countryCode)
                                } else {
                                    Log.w(TAG, "No address found for location")
                                }
                            }
                        } catch (e: IOException) {
                            Log.e(TAG, "Geocoding failed: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"Coroutine cancelled (geocoder")
        geocoderJob?.cancel() // Cancel the job to prevent leaks
    }

    //check location is enabled and internet to ensure app does not crash when database is empty
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val isNetworkAvailable = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        return isLocationEnabled && isNetworkAvailable // Ensures both location & internet work
    }
}

private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val LOCATION_PERMISSION_INDEX = 0
private const val NAVIGATE_FROM_UPCOMING_RELEASES = 0