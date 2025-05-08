package com.example.watchlist.moviedetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.watchlist.base.BaseViewModel
import com.example.watchlist.data.repository.MovieRepository
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TitleDetailViewModel(app: Application, val datasource: MovieRepository) : BaseViewModel(app) {

    companion object {
        val TAG = "MovieDetailDebug"
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel on cleared scope cleared")
    }


    private val _logoUrl = MutableLiveData<String?>()
    val logoUrl: LiveData<String?> = _logoUrl

    fun fetchStreamingServiceLogo(id: Int?) {
        viewModelScope.launch {
            try {
                val logoUrl = datasource.getStreamingServiceLogoRepo(id)
                Log.d(TAG, "TitleDetailViewModel streaming service logo $logoUrl")
                _logoUrl.postValue(logoUrl)
            } catch (e: Exception) {
                Log.d(TAG, "Error fetching streaming service logo ${e.message}", e)
            }
        }
        Log.d(TAG, "viewModelScope.launch after BLOCK ${viewModelScope.isActive}")
    }

    fun setWatchListTrue(titleId: Int) {
        viewModelScope.launch {
            try {
                datasource.addToWatchList(titleId)
                val title = datasource.getMovieById(titleId)
                Log.d(TAG,"After db update ${title.title} watchlist: ${title.watchlist} id ${title.id}")
            } catch (e: Exception) {
                Log.d(TAG, "Error adding to watch list ${e.message}", e)
            }
        }
    }

    fun setWatchListFalse(titleId: Int) {
        viewModelScope.launch {
            try {
                datasource.removeFromWatchList(titleId)
                val title = datasource.getMovieById(titleId)
                Log.d(TAG,"After db update REMOVE ${title.title} watchlist: ${title.watchlist} id ${title.id}")
            } catch (e: Exception) {
                Log.d(TAG, "Error adding to watch list ${e.message}", e)
            }
        }
    }
}