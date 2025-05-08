package com.example.watchlist.data.local.movies

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param title      title of movie
 * @param service    name of streaming service
 * @param type       format of movie such as tv or movie
 * @param date       date title is released by service
 * @param watchlist  title is saved to watchlist or not
 */

@Entity(tableName = "movie_table")
data class DatabaseMovie(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo (name = "title") var title: String,
    @ColumnInfo(name = "sourceId") var sourceId: Int,
    @ColumnInfo(name = "service") var service: String?,
    @ColumnInfo(name = "type") var type: String?,
    @ColumnInfo(name = "release_date") var date: String?,
    @ColumnInfo(name = "watchlist") var watchlist: Int?
)