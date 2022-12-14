package com.fasa.ziptechdevmovieapp.models

data class MovieResponse(
    val page: Int,
    val results: MutableList<Movie>,
    val total_pages: Int,
    val total_results: Int
)