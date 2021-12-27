package com.example.tmdbclient.movie.list.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmdbclient.R
import com.example.tmdbclient.shared.TmdbBasePaths
import com.example.tmdbclient.movie.list.logic.MovieListRepository

class MovieListAdapter(
    var movies: List<MovieListRepository.Movie>,
    private val clickListener: (MovieListRepository.Movie) -> Unit)
    : RecyclerView.Adapter<MovieListAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_movie_item,
                parent,
                false
            )
        return MovieViewHolder(view)
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        return holder.bind(movies[position])
    }

    inner class MovieViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {

        fun bind(movie: MovieListRepository.Movie,) {
            itemView.setOnClickListener { clickListener(movie) }

            itemView.findViewById<TextView>(R.id.title).text = movie.title
            Glide
                .with(itemView)
                .load(TmdbBasePaths.TMDB_POSTER_W300 + movie.posterPath)
                .into(itemView.findViewById<ImageView>(R.id.poster))
        }
    }
}