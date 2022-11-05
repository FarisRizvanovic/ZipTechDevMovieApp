package com.fasa.ziptechdevmovieapp.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fasa.ziptechdevmovieapp.R
import com.fasa.ziptechdevmovieapp.database.MoviesDatabase
import com.fasa.ziptechdevmovieapp.databinding.FragmentAllMoviesBinding
import com.fasa.ziptechdevmovieapp.databinding.FragmentMovieDetailsBinding
import com.fasa.ziptechdevmovieapp.repository.MoviesRepository
import com.fasa.ziptechdevmovieapp.ui.MainActivity
import com.fasa.ziptechdevmovieapp.ui.viewmodelfactories.MainViewModelFactory
import com.fasa.ziptechdevmovieapp.ui.viewmodelfactories.MovieDetailsViewModelFactory
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MainViewModel
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MovieDetailsViewModel
import com.fasa.ziptechdevmovieapp.util.Constants.Companion.BASE_IMAGE_URL


class MovieDetailsFragment : Fragment() {

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    private var isFavourite = false

    private val viewModel: MovieDetailsViewModel by lazy {
        val db = MoviesDatabase(requireContext())
        val moviesRepository = MoviesRepository(db)
        val viewModelProviderFactory = MovieDetailsViewModelFactory(moviesRepository)
        ViewModelProvider(this, viewModelProviderFactory)[MovieDetailsViewModel::class.java]
    }

    val args: MovieDetailsFragmentArgs by navArgs()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        val view = binding.root

        val movie = args.movie

        viewModel.checkIfMovieIsInFavourites(movie.id)

        Glide.with(this).load(BASE_IMAGE_URL + movie.poster_path)
            .into(binding.moviePoster)

        movie.apply {
            binding.movieName.text = title
            binding.originalLanguage.text = "Original language: $original_language"
            binding.releaseDate.text = "Release date: $release_date"
            binding.voteCount.text = "Vote count: ${vote_count.toString()}"
            binding.voteAverage.text = "Vote average: ${vote_average.toString()}"
            binding.overview.text = overview
        }

        viewModel.isInFavourites.observe(viewLifecycleOwner, Observer { isInFavourites ->
            isFavourite = isInFavourites
            if (isInFavourites) {
                binding.btnAddToFavourite.setImageDrawable(activity?.let {
                    ContextCompat.getDrawable(
                        it, R.drawable.ic_full_heart
                    )
                })
            } else {
                binding.btnAddToFavourite.setImageDrawable(activity?.let {
                    ContextCompat.getDrawable(
                        it, R.drawable.ic_empty_heart
                    )
                })
            }
        })

        binding.btnAddToFavourite.setOnClickListener {
            if (isFavourite) {
                viewModel.deleteMovie(movie)
                binding.btnAddToFavourite.setImageDrawable(activity?.let {
                    ContextCompat.getDrawable(
                        it, R.drawable.ic_empty_heart
                    )
                })
                isFavourite = false
            } else {
                viewModel.saveMovie(movie)
                binding.btnAddToFavourite.setImageDrawable(activity?.let {
                    ContextCompat.getDrawable(
                        it, R.drawable.ic_full_heart
                    )
                })
                isFavourite = true
            }
        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}