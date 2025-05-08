package com.example.watchlist.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.watchlist.utils.SingleLiveEvent

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {
    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showNoData: MutableLiveData<Boolean> = MutableLiveData()

    private val _showNoInternetMessage = MutableLiveData<Boolean>()
    val showNoInternetMessage: LiveData<Boolean> get() = _showNoInternetMessage

    fun triggerNoInternetMessage() {
        _showNoInternetMessage.value = true
    }

    fun resetNoInternetMessage() {
        _showNoInternetMessage.value = false
    }

    private val _showEmptyWatchlistMessage = MutableLiveData<Boolean>()
    val showEmptyWatchlistMessage: LiveData<Boolean> get() = _showEmptyWatchlistMessage

//    fun triggerEmptyWatchlistMessage() {
//        _showEmptyWatchlistMessage.value = true
//    }

    fun resetEmptyWatchlistMessage() {
        _showEmptyWatchlistMessage.value = false
    }



}