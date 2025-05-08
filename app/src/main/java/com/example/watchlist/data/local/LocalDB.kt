package com.example.watchlist.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.watchlist.data.local.movies.MovieDao
import com.example.watchlist.data.local.streamingService.ServiceDao

/**
 * Singleton class that is used to create a movie database
 */
object LocalDB {

    /**
     * Static method that creates a movie class and returns the DAOs of the movie and service
     */
    fun createMovieDao(context: Context): Pair<MovieDao, ServiceDao> {
        val database =  Room.databaseBuilder(
            context.applicationContext,
            MovieDatabase::class.java, name="movies.db")
            //.addMigrations(MIGRATION_3_4)
            .build()

        val movieDao = database.movieDao()
        val serviceDao = database.serviceDao()

        Log.i("WatchListApp","movieDao created successfully ${movieDao} and ${serviceDao}")

        return Pair(movieDao, serviceDao)
    }
}