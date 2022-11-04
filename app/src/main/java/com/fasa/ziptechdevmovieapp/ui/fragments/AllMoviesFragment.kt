package com.fasa.ziptechdevmovieapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.fasa.ziptechdevmovieapp.R
import com.fasa.ziptechdevmovieapp.databinding.FragmentAllMoviesBinding
import com.fasa.ziptechdevmovieapp.ui.MainActivity
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MainViewModel
import com.fasa.ziptechdevmovieapp.util.Resource
import retrofit2.Response

class AllMoviesFragment : Fragment() {

    val TAG = "AllMoviesFragment"

    private var _binding: FragmentAllMoviesBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllMoviesBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = (activity as MainActivity).viewModel


        viewModel.mostPopularMovies.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    response.data?.let { moviesResponse ->
                        binding.testText.text = moviesResponse.toString()
                    }
                }
                is Resource.Error -> {
                    response.message?.let {message->
                        Log.e(TAG, "Error: $message", )
                        
                    }
                }
                is Resource.Loading->{
                    Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()
                }

            }
        })




        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}