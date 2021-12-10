package com.example.tmdbclient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmdbclient.TmdbBasePaths.TMDB_POSTER_W300

class MovieListAdapter(
    private val movies: List<Movie>,
    private val clickListener: (Movie) -> Unit)
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

        fun bind(movie: Movie,) {
            itemView.setOnClickListener { clickListener(movie) }

            itemView.findViewById<TextView>(R.id.title).text = movie.title
            Glide
                .with(itemView)
                .load(TMDB_POSTER_W300 + movie.posterPath)
                .into(itemView.findViewById<ImageView>(R.id.poster))
        }
    }
}

class MovieListFragment : Fragment(R.layout.fragment_movie_list) {

    private val listViewModel : MovieListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.movie_list).apply {
            layoutManager = GridLayoutManager(context, 2)
        }

        val onMovieClick: (Movie) -> Unit = { movie ->
            val amount = movie.id
            val action = MovieListFragmentDirections.showMovieDetails(amount)
            findNavController().navigate(action)
        }

        listViewModel.movieList.observe(viewLifecycleOwner) { movieList ->
            recyclerView.adapter = MovieListAdapter(movieList, onMovieClick)
        }
    }
}