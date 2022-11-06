package com.fasa.ziptechdevmovieapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AllMoviesFragment : Fragment() {

    val TAG = "AllMoviesFragment"

    var genre: String = ""
    var sortBy: String = "popularity.desc"

    lateinit var toggle: ActionBarDrawerToggle

    private var _binding: FragmentAllMoviesBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    lateinit var moviesAdapter: MoviesAdapter

    lateinit var menu: Menu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllMoviesBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.sortBy.setText(getString(R.string.popularity))
        binding.sortOrder.setText(getString(R.string.descending))

        viewModel = (activity as MainActivity).viewModel
        setupRecyclerView()

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        val actionbar = (activity as AppCompatActivity).supportActionBar!!
        actionbar.title = "All Genres"

        toggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            binding.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        menu = binding.navigation.menu


        var job: Job? = null

        viewModel.genres.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { genresResponse ->
                        job?.cancel()
                        job = MainScope().launch {
                            for (genre in genresResponse.genres) {
                                menu.add(1, genre.id, menu.size(), genre.name)
                            }
                            menu.setGroupCheckable(1, true, false)

                            if (viewModel.selectedGenre != null) {
                                menu.getItem(viewModel.selectedGenre!!)?.let {
                                    it.isChecked = true
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "Genres Error!")
                }
                is Resource.Loading -> {
                    Log.d(TAG, "Genres Loading...")
                }
            }
        })


        binding.navigation.setNavigationItemSelectedListener {
            val title = it.title.toString()
            val id = it.itemId.toString()

            if (id == R.id.allGenres.toString()) {
                viewModel.shouldReset = true
                genre = ""
                viewModel.getMostPopularMovies(genre, sortBy)
            } else {
                viewModel.shouldReset = true
                genre = id
                viewModel.getMostPopularMovies(genre, sortBy)
            }

            actionbar.title = title
            viewModel.selectedGenre = it.order
            binding.moviesRecView.smoothScrollToPosition(0)
            binding.drawerLayout.closeDrawers()

            return@setNavigationItemSelectedListener true
        }


        moviesAdapter.setOnItemClickListener { movie ->
            val action =
                AllMoviesFragmentDirections.actionAllMoviesFragmentToMovieDetailsFragment(movie)
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

    private fun setupSortViews() {
        val sortBy = resources.getStringArray(R.array.sort_by)
        val sortOrder = resources.getStringArray(R.array.sort_order)

        val sortByAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            sortBy
        )

        val sortOrderAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            sortOrder
        )

        binding.sortBy.setAdapter(sortByAdapter)
        binding.sortOrder.setAdapter(sortOrderAdapter)

        binding.sortBy.setOnItemClickListener { _, _, _, _ ->
            recallData()
        }

        binding.sortOrder.setOnItemClickListener { _, _, _, _ ->
            recallData()
        }
    }

    private fun recallData() {
        val _sortBy = binding.sortBy.text.toString().lowercase().replace(" ", "_")
        var _sortOrder = binding.sortOrder.text.toString().substring(0, 3).lowercase()
        if (_sortOrder == "des") {
            _sortOrder +="c"
        }
        sortBy = "$_sortBy.$_sortOrder"
        viewModel.shouldReset = true
        viewModel.getMostPopularMovies(genre, sortBy)
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
                viewModel.getMostPopularMovies(genre, sortBy)
                isScrolling = false
            } else {
                binding.moviesRecView.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupSortViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}







