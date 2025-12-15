package com.delivery.setting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delivery.setting.databinding.ItemSearchResultBinding
import com.delivery.setting.model.SearchResult

class SearchResultAdapter(
    private val onItemClick: (SearchResult) -> Unit,
) : ListAdapter<SearchResult, SearchResultAdapter.SearchResultViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchResultViewHolder {
        val binding =
            ItemSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SearchResultViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    inner class SearchResultViewHolder(
        private val binding: ItemSearchResultBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(item: SearchResult) {
            binding.apply {
                tvPlaceName.text = item.name
                tvPlaceAddress.text = item.address
            }
        }
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<SearchResult>() {
                override fun areItemsTheSame(
                    oldItem: SearchResult,
                    newItem: SearchResult,
                ): Boolean {
                    return oldItem.placeId == newItem.placeId
                }

                override fun areContentsTheSame(
                    oldItem: SearchResult,
                    newItem: SearchResult,
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
