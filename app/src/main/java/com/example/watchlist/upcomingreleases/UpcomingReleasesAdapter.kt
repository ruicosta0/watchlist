package com.example.watchlist.upcomingreleases

import android.util.Log
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.watchlist.R
import com.example.watchlist.base.BaseRecyclerViewAdapter
import com.example.watchlist.base.MovieListener
import com.example.watchlist.data.domain.Movie

class MoviesListAdapter(
    callBack: ((selectedMovie: Movie) -> Unit)) : BaseRecyclerViewAdapter<Movie>(callBack) {

    override fun getLayoutRes(viewType: Int) = R.layout.it_movie //remember var in xml is called item (Movie class in Domain)
    }