package com.example.watchlist.launch

import android.app.Application
import com.example.watchlist.base.BaseViewModel
import com.example.watchlist.data.repository.MovieRepository

class LaunchViewModel(app: Application) : BaseViewModel(app) {

    companion object {
        val TAG = "WatchListApp"
    }
}

//val datasource: MovieRepository