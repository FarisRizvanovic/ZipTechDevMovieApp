package com.fasa.ziptechdevmovieapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasa.ziptechdevmovieapp.adapters.MoviesAdapter
import com.fasa.ziptechdevmovieapp.databinding.FragmentSearchMoviesBinding
import com.fasa.ziptechdevmovieapp.ui.MainActivity
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MainViewModel
import com.fasa.ziptechdevmovieapp.util.Constants
import com.fasa.ziptechdevmovieapp.util.Constants.Companion.SEARCH_MOVIES_TIME_DELAY
import com.fasa.ziptechdevmovieapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchMoviesFragment : Fragment() {

    private var _binding: FragmentSearchMoviesBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    private lateinit var moviesAdapter: MoviesAdapter
    private val TAG = "SearchMoviesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchMoviesBinding.inflate(inflater, container, false)
        val view = binding.root

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        val actionbar = (activity as AppCompatActivity).supportActionBar!!
        actionbar.title = "Search"

        viewModel = (activity as MainActivity).viewModel
        setupRecyclerView()


        var job: Job? = null
        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_MOVIES_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        val lastQuery = viewModel.lastQuery
                        if (lastQuery != editable.toString()){
                            viewModel.lastQuery = editable.toString()
                            viewModel.shouldReset = true
                            viewModel.searchMovies(editable.toString())
                        }

                    }
                }
            }
        }

        moviesAdapter.setOnItemClickListener { movie->
            val action = SearchMoviesFragmentDirections.actionSearchMoviesFragmentToMovieDetailsFragment(movie)
            findNavController().navigate(action)
        }

        viewModel.searchMovies.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { moviesResponse ->
                        moviesAdapter.differ.submitList(moviesResponse.results.toList())

                        val totalPages = moviesResponse.total_results / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchMoviesPage == totalPages
                        binding.searchPlaceholder.visibility = View.GONE
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
        binding.searchPaginationProgressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.searchPaginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setupRecyclerView() {
        moviesAdapter = MoviesAdapter()
        binding.searchMoviesRecView.apply {
            adapter = moviesAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchMoviesFragment.scrollListener)
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
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.searchMovies(binding.etSearch.text.toString())
                isScrolling = false
            } else {
                binding.searchMoviesRecView.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}