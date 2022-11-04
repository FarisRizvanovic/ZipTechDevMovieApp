package com.fasa.ziptechdevmovieapp.ui.viewmodels

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
) : ViewModel(){

    val mostPopularMovies : MutableLiveData<Resource<MovieResponse>> = MutableLiveData()
    private val mostPopularMoviesPage = 1

    init {
        getMostPopularMovies()
    }

    private fun getMostPopularMovies(){
        viewModelScope.launch {
            mostPopularMovies.postValue(Resource.Loading())
            val response = moviesRepository.getMostPopularMovies(mostPopularMoviesPage)
            mostPopularMovies.postValue(handleMostPopularMoviesResponse(response))
        }
    }

    private fun handleMostPopularMoviesResponse(response: Response<MovieResponse>) : Resource<MovieResponse>{
        if(response.isSuccessful){
            response.body()?.let {resultResponse->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

}