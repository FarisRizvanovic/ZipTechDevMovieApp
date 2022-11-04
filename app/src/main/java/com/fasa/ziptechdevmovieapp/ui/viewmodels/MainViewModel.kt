package com.fasa.ziptechdevmovieapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasa.ziptechdevmovieapp.models.MovieResponse
import com.fasa.ziptechdevmovieapp.repository.MoviesRepository
import com.fasa.ziptechdevmovieapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    val mostPopularMovies: MutableLiveData<Resource<MovieResponse>> = MutableLiveData()
    var mostPopularMoviesPage = 1
    var mostPopularMoviesResponse: MovieResponse? = null

    val searchMovies: MutableLiveData<Resource<MovieResponse>> = MutableLiveData()
    var searchMoviesPage = 1
    var searchMoviesResponse: MovieResponse? = null
    var oldSearchQuery: String? = null
    var newSearchQuery: String? = null

    init {
        getMostPopularMovies()
    }

    fun getMostPopularMovies() {
        viewModelScope.launch {
            mostPopularMovies.postValue(Resource.Loading())
            val response = moviesRepository.getMostPopularMovies(mostPopularMoviesPage)
            mostPopularMovies.postValue(handleMostPopularMoviesResponse(response))
        }
    }

    private fun handleMostPopularMoviesResponse(response: Response<MovieResponse>): Resource<MovieResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                mostPopularMoviesPage++
                if (mostPopularMoviesResponse == null) {
                    mostPopularMoviesResponse = resultResponse
                } else {
                    val oldMovies = mostPopularMoviesResponse?.results
                    val newMovies = resultResponse.results
                    oldMovies?.addAll(newMovies)
                }
                return Resource.Success(mostPopularMoviesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    fun searchMovies(queryText : String, resetData: Boolean) {
        viewModelScope.launch {
            searchMovies.postValue(Resource.Loading())
            val response = moviesRepository.searchMovies(queryText, searchMoviesPage)
            searchMovies.postValue(handleSearchMoviesResponse(response, resetData))
        }
    }

    private fun handleSearchMoviesResponse(response: Response<MovieResponse>, resetData: Boolean): Resource<MovieResponse> {
        if (response.isSuccessful) {

            response.body()?.let { resultResponse ->
                searchMoviesPage++
                if (searchMoviesResponse == null || resetData) {
                    searchMoviesPage = 1
                    searchMoviesResponse = resultResponse
                } else {
                    val oldMovies = searchMoviesResponse?.results
                    val newMovies = resultResponse.results
                    oldMovies?.addAll(newMovies)
                }
                return Resource.Success(searchMoviesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }



//    fun searchMovies(queryText : String) {
//        viewModelScope.launch {
//            searchMovies.postValue(Resource.Loading())
//            val response = moviesRepository.searchMovies(queryText, searchMoviesPage)
//            searchMovies.postValue(handleSearchMoviesResponse(response))
//        }
//    }

//    private fun handleSearchMoviesResponse(response: Response<MovieResponse>): Resource<MovieResponse> {
//        if (response.isSuccessful) {
//
//            response.body()?.let { resultResponse ->
//                searchMoviesPage++
//                if (searchMoviesResponse == null) {
//                    searchMoviesResponse = resultResponse
//                } else {
//                    val oldMovies = searchMoviesResponse?.results
//                    val newMovies = resultResponse.results
//                    oldMovies?.addAll(newMovies)
//                }
//                return Resource.Success(searchMoviesResponse ?: resultResponse)
//            }
//        }
//        return Resource.Error(response.message())
//    }

}