package com.example.watchlist

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.watchlist.data.local.LocalDB
//import com.example.watchlist.data.local.LocalDataSource
import com.example.watchlist.data.network.RemoteDataSource
import com.example.watchlist.data.network.RetrofitInstance
import com.example.watchlist.data.repository.MovieRepository
import com.example.watchlist.launch.LaunchViewModel
import com.example.watchlist.moviedetail.TitleDetailViewModel
import com.example.watchlist.upcomingreleases.UpcomingReleasesViewModel
import com.example.watchlist.work.RefreshDataWorker
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import java.util.concurrent.TimeUnit

class MyApp : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    companion object {
        val TAG = "WatchListApp"
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this) //possible to move to Koin declaration for testing if req
        Log.i("WatchListApp", "App created")
        delayedInit()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            viewModel { //lifecycle aware, tied to launchFragment lifecycle (use single declaration for app wide globally shared logic)
                LaunchViewModel(
                    get(named("application")),
                    //get(named("datasource"))
                )
            }
            single { //single as can be shared across app, not tied to lifecycle(?)
                UpcomingReleasesViewModel(
                    get(named("application")),
                    get(named("datasource"))
                )
            }
            single {
                TitleDetailViewModel(
                    get(named("application")),
                    get(named("datasource"))
                )
            }
            single(named("application")) {androidApplication()}

            single(named("dao")) { LocalDB.createMovieDao(this@MyApp) } //create movie dao
            single { RetrofitInstance.retrofit}
            single (named("remoteDataSource")) {RemoteDataSource()}
            single (named("datasource")) {MovieRepository(get(named("remoteDataSource")), get(named("dao")))}.also{Log.i(TAG, "single movierepo created")}
        }



        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }

    private fun delayedInit() {
        applicationScope.launch {
            setUpRecurringWork()
        }
    }

    private fun setUpRecurringWork() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(
            1,
            TimeUnit.DAYS
        )
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            RefreshDataWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}