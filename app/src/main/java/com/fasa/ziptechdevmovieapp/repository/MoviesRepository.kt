package com.fasa.ziptechdevmovieapp.repository

import com.fasa.ziptechdevmovieapp.api.RetrofitInstance

class MoviesRepository() {

    suspend fun getMostPopularMovies(page: Int) =
        RetrofitInstance.api.getMostPopularMovies(page = page)

    suspend fun searchMovies(queryText: String, page: Int)=
        RetrofitInstance.api.searchMovies(queryText, page)
}