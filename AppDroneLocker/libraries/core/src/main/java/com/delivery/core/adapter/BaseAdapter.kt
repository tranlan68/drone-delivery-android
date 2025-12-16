package com.delivery.core.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, ItemBinding : ViewBinding> : ListAdapter<T, BaseAdapter<T, ItemBinding>.BaseViewHolder>(
    object : DiffUtil.ItemCallback<T>() {


        override fun getChangePayload(oldItem: T & Any, newItem: T & Any): Any? {
            return Bundle()
        }

        override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
            return false
        }
    }) {

    protected abstract val itemLayoutRes: Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding: ItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), itemLayoutRes, parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bindSingleTime(holder, position)
        bindMultiTime(holder, position)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.firstOrNull() is Bundle) {
            bindMultiTime(holder, position)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    protected abstract fun bindSingleTime(holder: BaseViewHolder, position: Int)

    protected abstract fun bindMultiTime(holder: BaseViewHolder, position: Int)

    override fun submitList(list: List<T>?) {
        super.submitList(list?.toMutableList())
    }

    inner class BaseViewHolder(val itemBinding: ItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

}