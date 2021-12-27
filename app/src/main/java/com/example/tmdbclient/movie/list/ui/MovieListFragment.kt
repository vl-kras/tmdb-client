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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tmdbclient.shared.PagingFooterAdapter
import com.example.tmdbclient.databinding.FragmentMovieListBinding
import com.example.tmdbclient.movie.list.logic.MovieListRepository
import com.example.tmdbclient.tvshow.details.TvShowDetailsState
import com.google.android.material.snackbar.Snackbar
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


//TODO add Paging
class MovieListFragment : Fragment() {

    private val listViewModel : MovieListViewModel by viewModels()

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding does not exist")

    // FIXME: 24-Dec-21  
//    private val viewBinding: FragmentMovieListBinding by viewBinding(FragmentMovieListBinding::inflate)

    private val contentAdapter: MovieListAdapter by lazy {
        MovieListAdapter(emptyList()) { movie ->
            val action = MovieListFragmentDirections.showMovieDetails(movie.id)
            findNavController().navigate(action)
        }
    }

    private val footerAdapter: PagingFooterAdapter by lazy {
        PagingFooterAdapter {
            val action = MovieListState.Action.LoadMore
            listViewModel.handleAction(action)
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
            is InitialState -> {
                val action = MovieListState.Action.LoadInitial
                listViewModel.handleAction(action)
            }
            is Display -> {
                val oldSize = contentAdapter.itemCount
                contentAdapter.movies = state.movies
                val newSize = contentAdapter.itemCount
                contentAdapter.notifyItemRangeInserted(oldSize, newSize-oldSize)

                state.error?.let { exception ->
                    Snackbar.make(view!!, exception.message ?: "Something went wrong", Snackbar.LENGTH_SHORT).show()
                }
            }
            is Error -> {
                binding.statusMessage.text = state.exception.localizedMessage
            }
        }
    }

    private fun displayState(state: MovieListState) {

        with(binding) {
            movieList.isVisible = state is Display

            loadingIndicator.isVisible = listViewModel.isLoading()

            retryButton.isVisible = state is Error
            statusMessage.isVisible = state is Error
        }
    }
}