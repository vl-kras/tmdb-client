package com.example.tmdbclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MovieListAdapter(
    private val movies: List<Movie>,
    private val clickListener: (Movie) -> Unit)
    : RecyclerView.Adapter<MovieListAdapter.MovieViewHolder>() {

    val TMDB_POSTER_W780 = "https://image.tmdb.org/t/p/w780"

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

        fun bind(movie: Movie,) {
            itemView.setOnClickListener { clickListener(movie) }

            itemView.findViewById<TextView>(R.id.title).text = movie.title
            Glide.with(itemView).load(TMDB_POSTER_W780 + movie.posterPath).into(itemView.findViewById<ImageView>(R.id.poster))
        }
    }
}

class MovieFragment : Fragment(R.layout.fragment_movie) {

    private val viewModel : MovieViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.movieListView).apply {
            layoutManager = GridLayoutManager(context, 2)
        }

        viewModel.movieList.observe(viewLifecycleOwner) {
            recyclerView.apply {
                adapter = MovieListAdapter(it) { Toast.makeText(context, it.overview, Toast.LENGTH_SHORT).show() }
            }
        }
    }
}