package com.example.watchlist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.watchlist.data.local.movies.DatabaseMovie
import com.example.watchlist.data.local.movies.MovieDao
import com.example.watchlist.data.local.streamingService.RegionEntity
import com.example.watchlist.data.local.streamingService.ServiceDao
import com.example.watchlist.data.local.streamingService.ServiceEntity


/**
 * Room database that contains the movie database
 */

@Database(entities = [DatabaseMovie::class, ServiceEntity::class, RegionEntity::class], version = 4, exportSchema = false)
abstract class MovieDatabase: RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun serviceDao(): ServiceDao
}