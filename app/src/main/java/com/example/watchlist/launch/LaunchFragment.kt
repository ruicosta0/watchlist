package com.example.watchlist.launch

import android.os.Bundle
import android.text.Html
import android.text.Layout.Directions
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.watchlist.R
import com.example.watchlist.base.BaseFragment
import androidx.databinding.DataBindingUtil
import com.example.watchlist.base.NavigationCommand
import com.example.watchlist.databinding.FragmentLaunchBinding
import com.example.watchlist.upcomingreleases.UpcomingReleasesFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class LaunchFragment : BaseFragment() {

    private lateinit var binding: FragmentLaunchBinding
    override val _viewModel: LaunchViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_launch
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        //credit for using watchmode api
        val apiCredit = binding.apiCredit
        val apiHtmlText = "Streaming data powered by <a href='https://www.watchmode.com/'> Watchmode</a>"
        apiCredit.text = Html.fromHtml(apiHtmlText, Html.FROM_HTML_MODE_COMPACT)
        apiCredit.movementMethod = LinkMovementMethod.getInstance() // Makes links clickable

        //credit for using photography
        val photoCredit = binding.photoCreditCaption
        val htmlText = "Photo by <a href='https://unsplash.com/@grstocks'>GR Stocks</a> on <a href='https://unsplash.com/photos/q8P8YoR6erg'>Unsplash</a>"

        photoCredit.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
        photoCredit.movementMethod = LinkMovementMethod.getInstance() // Makes links clickable

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        //navigation buttons
        binding.viewUpcomingReleases.setOnClickListener {
            navigateToUpcomingReleases()
        }
        binding.goToWatchList.setOnClickListener{
            navigateToWatchList()
        }
    }

    private fun navigateToUpcomingReleases() {
//         Use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(NavigationCommand.To(LaunchFragmentDirections.toUpcomingReleasesFragment()))
    }

    private fun navigateToWatchList() {
//         Use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(NavigationCommand.To(LaunchFragmentDirections.toWatchList()))
    }
}