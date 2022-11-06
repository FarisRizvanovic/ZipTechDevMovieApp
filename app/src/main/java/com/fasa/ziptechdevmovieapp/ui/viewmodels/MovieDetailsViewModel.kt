package com.fasa.ziptechdevmovieapp.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasa.ziptechdevmovieapp.models.Movie
import com.fasa.ziptechdevmovieapp.repository.MoviesRepository
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    val isInFavourites: MutableLiveData<Boolean> = MutableLiveData()

    fun checkIfMovieIsInFavourites(movieId: Int) {
        viewModelScope.launch {
            val count = moviesRepository.isInFavourites(movieId)
            if (count > 0) {
                isInFavourites.postValue(true)
            } else {
                isInFavourites.postValue(false)
            }
        }

    }

    fun saveMovie(movie: Movie) {
        viewModelScope.launch {
            moviesRepository.upsert(movie)
        }
    }

    fun deleteMovie(movie: Movie){
        viewModelScope.launch {
            moviesRepository.deleteFavouriteMovie(movie)
        }
    }
}