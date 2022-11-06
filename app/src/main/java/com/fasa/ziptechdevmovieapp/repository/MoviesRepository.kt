package com.fasa.ziptechdevmovieapp.repository

import com.fasa.ziptechdevmovieapp.api.RetrofitInstance
import com.fasa.ziptechdevmovieapp.database.MoviesDatabase
import com.fasa.ziptechdevmovieapp.models.Movie

class MoviesRepository(
    private val db : MoviesDatabase
) {

    suspend fun getMostPopularMovies(genre : String, page: Int, sortBy:String) =
        RetrofitInstance.api.getMostPopularMovies(page = page, genreName = genre, sortBy = sortBy)

    suspend fun searchMovies(queryText: String, page: Int)=
        RetrofitInstance.api.searchMovies(queryText, page)

    suspend fun getAllGenres() = RetrofitInstance.api.getAllGenres()

    suspend fun upsert(movie : Movie) = db.getMovieDao().upsertFavouriteMovie(movie)

    suspend fun deleteFavouriteMovie(movie: Movie) = db.getMovieDao().deleteFavouriteMovie(movie)

    fun getAllFavouriteMovies() = db.getMovieDao().getAllFavouriteMovies()

    suspend fun isInFavourites(movieId: Int) = db.getMovieDao().isInFavourites(movieId)
}