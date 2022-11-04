package com.fasa.ziptechdevmovieapp.api

import com.fasa.ziptechdevmovieapp.models.MovieResponse
import com.fasa.ziptechdevmovieapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MoviesApi {

    @GET("discover/movie")
    suspend fun getMostPopularMovies(
        @Query("page") page : Int = 1,
        @Query("sort_by") sortBy : String = "popularity.desc",
        @Query("api_key") apiKey : String = API_KEY
    ) : Response<MovieResponse>
}