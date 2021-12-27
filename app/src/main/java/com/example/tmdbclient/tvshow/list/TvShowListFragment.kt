package com.example.tmdbclient.tvshow.list

import android.os.Bundle
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
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_W300
import com.example.tmdbclient.databinding.FragmentTvshowListBinding

class ShowListAdapter(
    private val shows: List<TvShowListRepository.TvShow>,
    private val clickListener: (TvShowListRepository.TvShow) -> Unit)
    : RecyclerView.Adapter<ShowListAdapter.ShowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.view_movie_item,
                parent,
                false
            )
        return ShowViewHolder(view)
    }

    override fun getItemCount(): Int {
        return shows.size
    }

    override fun onBindViewHolder(holder: ShowViewHolder, position: Int) {
        return holder.bind(shows[position])
    }

    inner class ShowViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {

        fun bind(show: TvShowListRepository.TvShow) {
            itemView.setOnClickListener { clickListener(show) }

            itemView.findViewById<TextView>(R.id.title).text = show.title
            Glide
                .with(itemView)
                .load(TMDB_POSTER_W300 + show.posterPath)
                .into(itemView.findViewById<ImageView>(R.id.poster))
        }
    }
}

//TODO add Paging
class TvShowListFragment : Fragment() {

    private val showListViewModel : TvShowListViewModel by viewModels()

    private var _binding: FragmentTvshowListBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is NULL")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTvshowListBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showList.layoutManager = GridLayoutManager(context, 2)

        binding.retryButton.setOnClickListener {
            val action = TvShowListState.Action.Load
            showListViewModel.handleAction(action)
        }

        showListViewModel.getState().observe(viewLifecycleOwner) { state ->
            observeState(state)
        }
    }

    private fun observeState(state: TvShowListState) {
        configureViews(state)
        displayState(state)
    }

    private fun configureViews(state: TvShowListState) {

        val onTvShowClick: (TvShowListRepository.TvShow) -> Unit = { show ->
            val action = TvShowListFragmentDirections.showTvshowDetails(show.id)
            findNavController().navigate(action)
        }

        when (state) {
            is TvShowListState.Initial ->{
                val action = TvShowListState.Action.Load
                showListViewModel.handleAction(action)
            }
            is TvShowListState.Display -> {
                binding.showList.adapter = ShowListAdapter(state.tvShowList, onTvShowClick)
            }
            is TvShowListState.Error -> {
                binding.statusMessage.text = state.exception.message
            }
        }
    }

    private fun displayState(state: TvShowListState) {
        with(binding) {
            showList.isVisible = state is TvShowListState.Display

            loadingIndicator.isVisible = state is TvShowListState.Loading

            retryButton.isVisible = state is TvShowListState.Error
            statusMessage.isVisible = state is TvShowListState.Error
        }
    }
}