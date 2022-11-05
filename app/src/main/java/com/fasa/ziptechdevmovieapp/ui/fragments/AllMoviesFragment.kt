package com.fasa.ziptechdevmovieapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasa.ziptechdevmovieapp.R
import com.fasa.ziptechdevmovieapp.adapters.MoviesAdapter
import com.fasa.ziptechdevmovieapp.databinding.FragmentAllMoviesBinding
import com.fasa.ziptechdevmovieapp.ui.MainActivity
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MainViewModel
import com.fasa.ziptechdevmovieapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.fasa.ziptechdevmovieapp.util.Resource
import retrofit2.Response

class AllMoviesFragment : Fragment() {

    val TAG = "AllMoviesFragment"

    private var _binding: FragmentAllMoviesBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    lateinit var moviesAdapter: MoviesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllMoviesBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = (activity as MainActivity).viewModel
        setupRecyclerView()

        moviesAdapter.setOnItemClickListener { movie->
            val action = AllMoviesFragmentDirections.actionAllMoviesFragmentToMovieDetailsFragment(movie)
            findNavController().navigate(action)
        }

        viewModel.mostPopularMovies.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { moviesResponse ->
                        moviesAdapter.differ.submitList(moviesResponse.results.toList())

                        val totalPages = moviesResponse.total_results / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.mostPopularMoviesPage == totalPages
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.e(TAG, "Error: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }

            }
        })


        return view
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setupRecyclerView() {
        moviesAdapter = MoviesAdapter()
        binding.moviesRecView.apply {
            adapter = moviesAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@AllMoviesFragment.scrollListener)
        }

    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getMostPopularMovies()
                isScrolling = false
            } else {
                binding.moviesRecView.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}







