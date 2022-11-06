package com.fasa.ziptechdevmovieapp.ui.viewmodelfactories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fasa.ziptechdevmovieapp.repository.MoviesRepository
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MovieDetailsViewModel
import java.lang.IllegalArgumentException

class MovieDetailsViewModelFactory(private val moviesRepository: MoviesRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieDetailsViewModel::class.java)) {
            return MovieDetailsViewModel(moviesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel!")
    }
}

