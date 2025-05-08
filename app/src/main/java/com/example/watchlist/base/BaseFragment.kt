package com.example.watchlist.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.watchlist.R
//import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment for common LiveData objects
 */

abstract class BaseFragment : Fragment() {

    abstract val _viewModel: BaseViewModel

    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showToast.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showSnackBar.observe(this, Observer {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showSnackBarInt.observe(this, Observer {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showNoInternetMessage.observe(this, Observer  { show ->
            if (show) {
                Toast.makeText(activity, R.string.internet_connectivity, Toast.LENGTH_SHORT).show()
                _viewModel.resetNoInternetMessage()
            }
        })
        _viewModel.showEmptyWatchlistMessage.observe(this, Observer  { show ->
            if (show) {
                Toast.makeText(activity, R.string.watchlist_empty, Toast.LENGTH_SHORT).show()
                _viewModel.resetEmptyWatchlistMessage()
            }
        })

        _viewModel.navigationCommand.observe(this, Observer { command ->
            when (command) {
                is NavigationCommand.To -> {
                    command.bundle?.let {
                        findNavController().navigate(command.directions.actionId, it) // pass bundle (for title detail)
                    } ?: findNavController().navigate(command.directions) // Default without bundle
                }
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        })
    }
}