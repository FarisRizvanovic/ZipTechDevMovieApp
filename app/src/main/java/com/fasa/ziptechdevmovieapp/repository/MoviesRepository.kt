package com.fasa.ziptechdevmovieapp.repository

import com.fasa.ziptechdevmovieapp.api.RetrofitInstance

class MoviesRepository() {

    suspend fun getMostPopularMovies(page : Int) =
        RetrofitInstance.api.getMostPopularMovies(page = page)
}