package com.fasa.ziptechdevmovieapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fasa.ziptechdevmovieapp.models.Movie

@Dao
interface MoviesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavouriteMovie(movie: Movie): Long

    @Query("SELECT * FROM movies")
    fun getAllFavouriteMovies(): LiveData<List<Movie>>

    @Query("SELECT COUNT(id) FROM movies where id=:movieId")
    suspend fun isInFavourites(movieId: Int) : Int

    @Delete
    suspend fun deleteFavouriteMovie(movie: Movie)
}