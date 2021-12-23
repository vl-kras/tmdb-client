package com.example.tmdbclient.movie.list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tmdbclient.shared.PagingFooterAdapter
import com.example.tmdbclient.databinding.FragmentMovieListBinding


//TODO add Paging
class MovieListFragment : Fragment() {

    private val listViewModel : MovieListViewModel by viewModels()

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding does not exist")

//    private val onMovieClick: (MovieListRepository.Movie) -> Unit =

    private val contentAdapter: MovieListAdapter by lazy {
        MovieListAdapter(emptyList()) { movie ->
            val action = MovieListFragmentDirections.showMovieDetails(movie.id)
            findNavController().navigate(action)
        }
    }

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

            listViewModel.handleAction(MovieListState.Action.LoadInitial)
        }

        binding.movieList.layoutManager = GridLayoutManager(context, 2)

        val onFooterClick: () -> Unit = {
            val action = MovieListState.Action.LoadMore
            listViewModel.handleAction(action)
        }

        val footerAdapter = PagingFooterAdapter(onFooterClick)
        binding.movieList.adapter = ConcatAdapter(contentAdapter, footerAdapter)

        listViewModel.getMovies().observe(viewLifecycleOwner) { state ->

            observeStateChanges(state)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun observeStateChanges(state: MovieListState) {
        configureViews(state)
        displayState(state)
    }

    private fun configureViews(state: MovieListState) {

        when (state) {
            is MovieListState.Initial -> {
                val action = MovieListState.Action.LoadInitial
                listViewModel.handleAction(action)
            }
            is MovieListState.Display -> {

                contentAdapter.movies = state.movies
                contentAdapter.notifyItemRangeInserted(contentAdapter.itemCount, 20)
            }
            is MovieListState.Error -> {
                binding.statusMessage.text = state.exception.localizedMessage
            }
        }
    }

    private fun displayState(state: MovieListState) {

        with(binding) {
            movieList.isVisible = state is MovieListState.Display

            loadingIndicator.isVisible = state is MovieListState.Loading

            retryButton.isVisible = state is MovieListState.Error
            statusMessage.isVisible = state is MovieListState.Error
        }
    }
}