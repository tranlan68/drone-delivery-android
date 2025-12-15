package com.delivery.setting.ui.createorder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.delivery.setting.R

class DropdownAdapter(
    context: Context,
    private val items: List<HubItem>
) : android.widget.ArrayAdapter<HubItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_dropdown, parent, false)

        val tv = view.findViewById<TextView>(R.id.tvName)
        tv.text = items[position].name

        return view
    }
}