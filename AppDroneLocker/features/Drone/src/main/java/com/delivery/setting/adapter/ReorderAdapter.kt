package com.delivery.setting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delivery.core.utils.getDimension
import com.delivery.setting.R
import com.delivery.setting.databinding.ItemReorderBinding
import com.delivery.setting.model.ReorderItem

class ReorderAdapter(
    private val onItemClick: (ReorderItem) -> Unit
) : ListAdapter<ReorderItem, ReorderAdapter.ReorderViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReorderViewHolder {
        val binding = ItemReorderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReorderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReorderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReorderViewHolder(
        private val binding: ItemReorderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(item: ReorderItem) {
            binding.apply {
                tvFromLocation.text = item.fromLocation
                tvFromAddress.text = item.fromAddress
                tvToLocation.text = item.toLocation
                tvToAddress.text = item.toAddress
                tvPackageType.text = item.packageType
                tvPackageWeight.text = item.packageWeight
                tvOrderStatus.text = item.status
                tvOderDate.text = binding.root.context.getString(com.delivery.core.R.string.string_date_order, item.orderDate)
            }
            val layoutParams =
                binding.clContainer.layoutParams as ViewGroup.MarginLayoutParams
            if (itemCount == 1) {
                layoutParams.setMargins(
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_8).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt()
                )
            } else {
                val marginStart =
                    if (position == 0)  binding.clContainer.context.getDimension(R.dimen.dimen_16) else {
                        binding.clContainer.context.getDimension(R.dimen.dimen_8)
                    }
                val marginEnd =
                    if (position == itemCount - 1)  binding.clContainer.context.getDimension(R.dimen.dimen_16) else {
                        binding.clContainer.context.getDimension(R.dimen.dimen_8)
                    }

                layoutParams.setMargins(
                    marginStart.toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_8).toInt(),
                    marginEnd.toInt(),
                    binding.clContainer.context.getDimension(R.dimen.dimen_16).toInt()
                )
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ReorderItem>() {
            override fun areItemsTheSame(oldItem: ReorderItem, newItem: ReorderItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ReorderItem, newItem: ReorderItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
