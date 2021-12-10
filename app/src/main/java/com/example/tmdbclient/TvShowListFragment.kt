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

class ShowListAdapter(
    private val shows: List<TvShow>,
    private val clickListener: (TvShow) -> Unit)
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

        fun bind(show: TvShow) {
            itemView.setOnClickListener { clickListener(show) }

            itemView.findViewById<TextView>(R.id.title).text = show.name
            Glide
                .with(itemView)
                .load(TMDB_POSTER_W300 + show.posterPath)
                .into(itemView.findViewById<ImageView>(R.id.poster))
        }
    }
}

class TvShowListFragment : Fragment(R.layout.fragment_tvshow_list) {

    private val showListViewModel : TvShowListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val recyclerView = view.findViewById<RecyclerView>(R.id.show_list).apply {
            layoutManager = GridLayoutManager(context, 2)
        }

        val onTvShowClick: (TvShow) -> Unit = { show ->
            val amount = show.id
            val action = TvShowListFragmentDirections.showTvshowDetails(amount)
            findNavController().navigate(action)
        }

        showListViewModel.showList.observe(viewLifecycleOwner) { tvShowList ->
            recyclerView.adapter = ShowListAdapter(tvShowList, onTvShowClick)
        }
    }
}