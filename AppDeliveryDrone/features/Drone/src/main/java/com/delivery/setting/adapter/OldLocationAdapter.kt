package com.delivery.setting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delivery.core.utils.getDimension
import com.delivery.setting.R
import com.delivery.setting.databinding.ItemOldLocationBinding
import com.delivery.setting.model.DeliveryLocation

class OldLocationAdapter(
    private val onItemClick: (DeliveryLocation) -> Unit,
) : ListAdapter<DeliveryLocation, OldLocationAdapter.ReorderViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ReorderViewHolder {
        val binding =
            ItemOldLocationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ReorderViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ReorderViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    inner class ReorderViewHolder(
        private val binding: ItemOldLocationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(item: DeliveryLocation) {
            val layoutParams =
                binding.clContainer.layoutParams as ViewGroup.MarginLayoutParams
            if (itemCount == 1) {
                layoutParams.setMargins(
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_8).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                )
            } else {
                val marginStart =
                    if (position == 0) {
                        binding.clContainer.context.getDimension(R.dimen.dimen_16)
                    } else {
                        binding.clContainer.context.getDimension(R.dimen.dimen_8)
                    }
                val marginEnd =
                    if (position == itemCount - 1) {
                        binding.clContainer.context.getDimension(R.dimen.dimen_16)
                    } else {
                        binding.clContainer.context.getDimension(R.dimen.dimen_8)
                    }

                layoutParams.setMargins(
                    marginStart.toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_8).toInt(),
                    marginEnd.toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                )
            }
            binding.apply {
                tvAddress.text = item.address
            }
        }
    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<DeliveryLocation>() {
                override fun areItemsTheSame(
                    oldItem: DeliveryLocation,
                    newItem: DeliveryLocation,
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: DeliveryLocation,
                    newItem: DeliveryLocation,
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
