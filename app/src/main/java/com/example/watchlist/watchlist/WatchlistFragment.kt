package com.example.watchlist.watchlist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.DataBindingUtil
import com.example.watchlist.R
import com.example.watchlist.base.BaseFragment
import com.example.watchlist.base.MovieListener
import com.example.watchlist.data.domain.Movie
import com.example.watchlist.databinding.FragmentWatchListBinding
import com.example.watchlist.upcomingreleases.MoviesListAdapter
import com.example.watchlist.upcomingreleases.UpcomingReleasesViewModel
import com.example.watchlist.utils.setup
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WatchlistFragment: BaseFragment()  {

    private lateinit var binding: FragmentWatchListBinding
    override val _viewModel: UpcomingReleasesViewModel by activityViewModel() //activity scoped to prevent losing view model after navigating
    private lateinit var adapter: MoviesListAdapter

    companion object {
        val TAG = "FragmentWatchListApp"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_watch_list
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        setupRecyclerView() //vertical movie list recycler

        binding.viewModel = _viewModel //attach xml variable viewmodel to _viewmodel

        setHasOptionsMenu(true) //required for menu

        //observe titles that are marked as being in watchlist
        _viewModel.moviesAll.observe(viewLifecycleOwner) { movies ->
            Log.d(TAG, "Raw movies list observed $movies.size")
            if (::adapter.isInitialized) {
                // Apply an additional filter locally in Fragment to show only titles in user's watchlist
                val watchlistFilteredList = movies.filter { movie ->
                    movie.watchlist == 1 //  watchlist = 1 is true
                }
                adapter.updateItems(watchlistFilteredList)
                Log.d(TAG, "Observer triggered: ${watchlistFilteredList.size} items in watchlist")
            } else {
                Log.e(TAG, "Adapter not initialized before updating items!")
            }
        }
        //alert user to no internet connectivity
        _viewModel.showNoInternetMessage.observe(viewLifecycleOwner) { show ->
            if (show) {
                Toast.makeText(context, R.string.internet_connectivity, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
       // Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
       // Log.d(TAG,"onResume refresh movies called") )
    }

    private fun setupRecyclerView() {
        //pass in click listener to adapter param
        val movieListener = MovieListener {
                movie -> showMovieOptionsDialog(movie)
        }
        adapter = MoviesListAdapter(movieListener::onClick) //do not put val in front otherwise is only declared locally and not fragment level
        binding.moviesRecyclerViewWatchlist.setup(adapter)
        Log.d(TAG, "RECYCLERVIEW AND ADAPTER SETUP COMPLETE")
    }

    //on selecting title user sees dialog and can remove title from watchlist, view title details (api call) or cancel dialog
    @SuppressLint("MissingInflatedId")
    fun showMovieOptionsDialog(movie: Movie) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_choose_an_action, null)
        val motionLayout = dialogView.findViewById<MotionLayout>(R.id.dialogBox) //not binding xml


        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
        dialog.show()

        //motionLayout.transitionToEnd()

        // Handle button clicks
        dialogView.findViewById<Button>(R.id.removeWatchlistBtn).setOnClickListener {
            _viewModel.handleMovieOptions(requireContext(), movie, MovieAction.REMOVE_FROM_WATCHLIST, NAVIGATE_FROM_WATCHLIST)
            motionLayout.transitionToStart()
            motionLayout.postDelayed({ dialog.dismiss() }, 500)
        }

        dialogView.findViewById<Button>(R.id.navigateToDetailsBtn).setOnClickListener {
            _viewModel.handleMovieOptions(requireContext(), movie, MovieAction.NAVIGATE_TO_DETAILS, NAVIGATE_FROM_WATCHLIST)
            motionLayout.transitionToStart()
            motionLayout.postDelayed({ dialog.dismiss() }, 500)
        }

        dialogView.findViewById<Button>(R.id.cancelBtn).setOnClickListener {
            motionLayout.transitionToStart()
            motionLayout.postDelayed({ dialog.dismiss() }, 500)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.watchlist_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    //user has options from menu to go back to upcoming releases fragment
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.go_to_releases) {
            _viewModel.navigateToUpcomingReleases() //go to watchlist
            return true
        }
        if (item.itemId != android.R.id.home) { //android.R.id.home is navigate up arrow
        }
        return false //allows handling onSupportNavigateUp()(R.id.home) to navigate up
    }
}

//actions in dialog box on tapping on titles
enum class MovieAction {
    REMOVE_FROM_WATCHLIST,
    NAVIGATE_TO_DETAILS,
    RETURN
}

//used to navigate to title details
private const val NAVIGATE_FROM_WATCHLIST = 1