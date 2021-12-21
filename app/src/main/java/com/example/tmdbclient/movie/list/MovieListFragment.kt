package com.example.tmdbclient.movie.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmdbclient.R
import com.example.tmdbclient.TmdbBasePaths.TMDB_POSTER_W300
import com.example.tmdbclient.databinding.FragmentMovieListBinding

class MovieListAdapter(
    private val movies: List<MovieListRepository.Movie>,
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
                .load(TMDB_POSTER_W300 + movie.posterPath)
                .into(itemView.findViewById<ImageView>(R.id.poster))
        }
    }
}

//TODO add Paging
class MovieListFragment : Fragment() {

    private val listViewModel : MovieListViewModel by viewModels()

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding does not exist")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.retryButton.setOnClickListener {

            listViewModel.handleAction(MovieListState.Action.Retry)
        }

        val movieList = binding.movieList
        movieList.apply {
            layoutManager = GridLayoutManager(context, 2)
        }

        val onMovieClick: (MovieListRepository.Movie) -> Unit = { movie ->
            val action = MovieListFragmentDirections.showMovieDetails(movie.id)
            findNavController().navigate(action)
        }


        listViewModel.getMovies().observe(viewLifecycleOwner) { state ->
            Log.d("BLABLA", "STATE IS $state")

            when (state) {
                is MovieListState.Display -> {
                    movieList.adapter = MovieListAdapter(state.movies, onMovieClick)
                }
                is MovieListState.Error -> {
                    binding.statusMessage.text = state.exception.localizedMessage
                }
            }

            with(binding) {
                movieList.isVisible = state is MovieListState.Display

                loadingIndicator.isVisible = state is MovieListState.Loading

                retryButton.isVisible = state is MovieListState.Error
                statusMessage.isVisible = state is MovieListState.Error
            }
        }


        listViewModel.handleAction(MovieListState.Action.LoadMovies)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}