package com.fasa.ziptechdevmovieapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fasa.ziptechdevmovieapp.R
import com.fasa.ziptechdevmovieapp.models.Result
import com.fasa.ziptechdevmovieapp.util.Constants.Companion.BASE_IMAGE_URL

class MoviesAdapter : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Result>() {
        override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_movie,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(BASE_IMAGE_URL + movie.poster_path)
                .into(findViewById(R.id.img_movie_icon))
            findViewById<TextView>(R.id.txt_movie_name).text = movie.title

            findViewById<TextView>(R.id.txt_release_date).text = movie.release_date
            findViewById<TextView>(R.id.txt_average_grade).text =
                "${movie.vote_average} (${movie.vote_count})"

        }
    }

    override fun getItemCount(): Int = differ.currentList.size


}