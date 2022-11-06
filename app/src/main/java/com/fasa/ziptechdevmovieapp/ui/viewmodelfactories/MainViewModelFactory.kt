package com.fasa.ziptechdevmovieapp.ui.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fasa.ziptechdevmovieapp.repository.MoviesRepository
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MainViewModel
import java.lang.IllegalArgumentException

class MainViewModelFactory(
    val app: Application,
    private val moviesRepository: MoviesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel(app, moviesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel!")
    }
}