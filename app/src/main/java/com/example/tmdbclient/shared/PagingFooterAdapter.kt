package com.example.tmdbclient.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tmdbclient.R

class PagingFooterAdapter(
    private val clickListener: () -> Unit
): RecyclerView.Adapter<PagingFooterAdapter.PagingFooterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagingFooterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.load_more_viewholder,
                parent,
                false
            )
        return PagingFooterViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagingFooterViewHolder, position: Int) {
        return holder.bind()
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class PagingFooterViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {

        fun bind() {
            itemView.setOnClickListener { clickListener() }
        }
    }
}