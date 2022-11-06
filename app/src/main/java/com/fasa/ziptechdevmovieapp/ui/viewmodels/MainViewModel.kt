package com.fasa.ziptechdevmovieapp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasa.ziptechdevmovieapp.MoviesApplication
import com.fasa.ziptechdevmovieapp.models.GenresResponse
import com.fasa.ziptechdevmovieapp.models.Movie
import com.fasa.ziptechdevmovieapp.models.MovieResponse
import com.fasa.ziptechdevmovieapp.repository.MoviesRepository
import com.fasa.ziptechdevmovieapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class MainViewModel(
    app: Application,
    private val moviesRepository: MoviesRepository
) : AndroidViewModel(app) {

    val mostPopularMovies: MutableLiveData<Resource<MovieResponse>> = MutableLiveData()
    var mostPopularMoviesPage = 1
    private var mostPopularMoviesResponse: MovieResponse? = null

    val searchMovies: MutableLiveData<Resource<MovieResponse>> = MutableLiveData()
    var searchMoviesPage = 1
    private var searchMoviesResponse: MovieResponse? = null

    val genres: MutableLiveData<Resource<GenresResponse>> = MutableLiveData()

    var shouldReset = false

    var selectedGenre: Int? = null

    var lastQuery = ""

    init {
        getMostPopularMovies("", "popularity.desc")
        getAllGenres()
    }

    /*
    Save movie to database
    */
    fun saveMovie(movie: Movie) {
        viewModelScope.launch {
            moviesRepository.upsert(movie)
        }
    }

    /*
    Get favourite movies from database
    */
    fun getFavouriteMovies() = moviesRepository.getAllFavouriteMovies()

    /*
    Delete movie from favourites database
    */
    fun deleteMovie(movie: Movie) {
        viewModelScope.launch {
            moviesRepository.deleteFavouriteMovie(movie)
        }
    }

    /*
    Get all available movie genres
    */
    private fun getAllGenres() {
        viewModelScope.launch {
            genres.postValue(Resource.Loading())
            safeGetGenres()
        }
    }

    private suspend fun safeGetGenres(){
        genres.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = moviesRepository.getAllGenres()
                genres.postValue(handleGenres(response))
            }else{
                genres.postValue(Resource.Error("No internet connection!"))
            }

        }catch (t : Throwable){
            when (t){
                is IOException -> genres.postValue(Resource.Error("Network failure!"))
                else -> genres.postValue(Resource.Error("Conversion error!"))
            }
        }
    }

    private fun handleGenres(response: Response<GenresResponse>): Resource<GenresResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }


    /*
    Get movies (default most popular movies) can be used to search for movies with
    genres and sorting with filters
     */
    fun getMostPopularMovies(genre: String, sortBy: String) {
        viewModelScope.launch {
            if (shouldReset) {
                mostPopularMoviesResponse = null
                shouldReset = false
                mostPopularMoviesPage = 1
            }
           safeGetMostPopularMovies(genre, sortBy)
        }
    }

    private suspend fun safeGetMostPopularMovies(genre: String, sortBy: String){
        mostPopularMovies.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response =
                    moviesRepository.getMostPopularMovies(genre, mostPopularMoviesPage, sortBy)
                mostPopularMovies.postValue(handleMostPopularMoviesResponse(response))
            }else{
                mostPopularMovies.postValue(Resource.Error("No internet connection!"))
            }

        }catch (t : Throwable){
            when (t){
                is IOException -> mostPopularMovies.postValue(Resource.Error("Network failure!"))
                else -> mostPopularMovies.postValue(Resource.Error("Conversion error!"))
            }
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


    /*
    Search for movies with movie name
     */
    fun searchMovies(queryText: String) {
        viewModelScope.launch {
            if (shouldReset) {
                searchMoviesResponse = null
                shouldReset = false
                searchMoviesPage = 1
            }
            safeSearchMovies(queryText)
        }
    }

    private suspend fun safeSearchMovies(queryText: String){
        searchMovies.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = moviesRepository.searchMovies(queryText, searchMoviesPage)
                searchMovies.postValue(handleSearchMoviesResponse(response))
            }else{
                searchMovies.postValue(Resource.Error("No internet connection!"))
            }

        }catch (t : Throwable){
            when (t){
                is IOException -> searchMovies.postValue(Resource.Error("Network failure!"))
                else -> searchMovies.postValue(Resource.Error("Conversion error!"))
            }
        }
    }

    private fun handleSearchMoviesResponse(
        response: Response<MovieResponse>,
    ): Resource<MovieResponse> {
        if (response.isSuccessful) {

            response.body()?.let { resultResponse ->
                searchMoviesPage++
                if (searchMoviesResponse == null) {
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


    /*
    Check for internet connection (supports API below and above 23)
     */
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<MoviesApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            //For api below 23
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }


}