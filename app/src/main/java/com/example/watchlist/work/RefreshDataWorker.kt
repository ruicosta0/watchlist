package com.example.watchlist.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.watchlist.data.repository.MovieRepository
import retrofit2.HttpException

class RefreshDataWorker( appContext: Context,
                         params: WorkerParameters,
                         private val repository: MovieRepository):
                        CoroutineWorker(appContext, params ) {

    companion object {
        val TAG = "WatchListApp"
        const val WORK_NAME = "RefreshDataWorker"
    }
    override suspend fun doWork(): Result {
        return try {
            repository.repoRefreshMovies()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "error refreshing data", e)
            Result.retry()
        }
    }
}