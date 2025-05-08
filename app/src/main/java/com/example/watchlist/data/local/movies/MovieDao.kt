package com.example.watchlist.data.local.movies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Defines methods for using the Movie class with Room
 * LiveData does not require suspend functions
 */

@Dao
interface MovieDao {
    // create new movie entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg movie: DatabaseMovie)

    //return all movies in the database
    @Query("select * from movie_table ORDER BY release_date ASC")
    fun getMovies() : LiveData<List<DatabaseMovie>>

    //return all movies in the database not live data
    @Query("select * from movie_table ORDER BY release_date ASC")
    fun getAllMovies() : List<DatabaseMovie>

    //return all movies in the database not live data
    @Query("select * from movie_table WHERE watchlist = 1 ORDER BY release_date ASC")
    fun getAllWatchlistTitles() : LiveData<List<DatabaseMovie>>

    //return all movies released today
    @Query("select * from movie_table WHERE release_date = :today" )
    fun getMovieByDate(today: String) : LiveData<List<DatabaseMovie>>

    //return all movies released from today and over the next seven days
    @Query("select * from movie_table WHERE release_date BETWEEN :today AND :todayPlusSeven ORDER BY release_date ASC")
    fun getNextSevenDaysMovies(today: String, todayPlusSeven: String): LiveData<List<DatabaseMovie>>

    //return all movies released from today onwards
    @Query("select * from movie_table WHERE release_date >= :today ORDER BY release_date ASC")
    fun getTodayOnwards(today: String): LiveData<List<DatabaseMovie>>

    // Set the watchlist to true
    @Query("UPDATE movie_table SET watchlist = 1 WHERE id = :movieId")
    suspend fun addToWatchlist(movieId: Int)

    @Query("select * from movie_table WHERE id = :titleId")
    suspend fun getMovieById(titleId: Int) : DatabaseMovie

    // Set the watchlist to false
    @Query("UPDATE movie_table SET watchlist = 0 WHERE id = :movieId")
    suspend fun removeFromWatchlist(movieId: Int)

    //delete contents of database
    @Query("DELETE FROM movie_table")
    suspend fun clear()

    //count rows in database
    @Query("SELECT COUNT(title) from movie_table")
    suspend fun getRowCount() : Int
}