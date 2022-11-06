package com.fasa.ziptechdevmovieapp.ui.fragments

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasa.ziptechdevmovieapp.R
import com.fasa.ziptechdevmovieapp.adapters.MoviesAdapter
import com.fasa.ziptechdevmovieapp.databinding.FragmentFavouriteMoviesBinding
import com.fasa.ziptechdevmovieapp.databinding.FragmentMovieDetailsBinding
import com.fasa.ziptechdevmovieapp.ui.MainActivity
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar

class FavouriteMoviesFragment : Fragment() {

    private var _binding: FragmentFavouriteMoviesBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: MainViewModel
    lateinit var moviesAdapter: MoviesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavouriteMoviesBinding.inflate(inflater, container, false)
        val view = binding.root

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        val actionbar = (activity as AppCompatActivity).supportActionBar!!
        actionbar.title = "Favourites"

        viewModel = (activity as MainActivity).viewModel

        setupRecyclerView()

        moviesAdapter.setOnItemClickListener { movie ->
            val action =
                FavouriteMoviesFragmentDirections.actionFavouriteMoviesFragmentToMovieDetailsFragment(
                    movie
                )
            findNavController().navigate(action)
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {


            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                setDelete(c, viewHolder, dX, dY, isCurrentlyActive)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val movie = moviesAdapter.differ.currentList[position]
                viewModel.deleteMovie(movie)
                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveMovie(movie)
                    }
                }.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.favouriteMoviesRecView)
        }

        viewModel.getFavouriteMovies().observe(viewLifecycleOwner, Observer {
            moviesAdapter.differ.submitList(it.toList())
        })

        return view
    }

    private fun setDelete(
        c: Canvas,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        isCurrentlyActive: Boolean
    ) {
        val mClearPaint = Paint()
        mClearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        val mBackground = ColorDrawable()
        val backgroundColor = Color.parseColor("#b80f0a")
        val deleteDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        val intrinsicWidth = deleteDrawable!!.intrinsicWidth
        val intrinsicHeight = deleteDrawable.intrinsicHeight
        val itemView = viewHolder.itemView
        val itemHeight = itemView.height
        val isCancelled = dX == 0f && !isCurrentlyActive
        if (isCancelled){
            c.drawRect(
                itemView.right+dX, itemView.top.toFloat(),
                itemView.right.toFloat(), itemView.bottom.toFloat(),mClearPaint
            )
            return
        }
        mBackground.color = backgroundColor
        mBackground.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        mBackground.draw(c)
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) /2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop - intrinsicHeight
        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteDrawable.draw(c)
    }

    private fun setupRecyclerView() {
        moviesAdapter = MoviesAdapter()
        binding.favouriteMoviesRecView.apply {
            adapter = moviesAdapter
            layoutManager = LinearLayoutManager(activity)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}