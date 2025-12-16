package com.delivery.setting.ui.orderhistory

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.delivery.setting.R

class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val tabs = mutableListOf<CustomTab>()
    private var selectedTabIndex = 0
    private var onTabSelectedListener: OnTabSelectedListener? = null

    init {
        orientation = HORIZONTAL
        setPadding(16, 16, 16, 16)
    }

    fun addTab(title: String) {
        val tabView = createTabView(title, tabs.size)
        tabs.add(CustomTab(title, tabView))
        addView(tabView)
        
        if (tabs.size == 1) {
            selectTab(0)
        }
    }

    private fun createTabView(title: String, index: Int): View {
        val tabView = LayoutInflater.from(context).inflate(R.layout.custom_tab_item, this, false)
        val textView = tabView.findViewById<TextView>(R.id.tvTabTitle)
        textView.text = title
        
        val layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        layoutParams.setMargins(8, 0, 8, 0)
        tabView.layoutParams = layoutParams
        
        tabView.setOnClickListener {
            selectTab(index)
            onTabSelectedListener?.onTabSelected(index)
        }
        
        return tabView
    }

    private fun selectTab(index: Int) {
        if (index == selectedTabIndex) return
        
        // Deselect previous tab
        if (selectedTabIndex < tabs.size) {
            val previousTab = tabs[selectedTabIndex].view
            previousTab.setBackgroundResource(R.drawable.tab_unselected_background)
            val previousTextView = previousTab.findViewById<TextView>(R.id.tvTabTitle)
            previousTextView.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        
        // Select new tab
        selectedTabIndex = index
        val currentTab = tabs[index].view
        currentTab.setBackgroundResource(R.drawable.tab_selected_background)
        val currentTextView = currentTab.findViewById<TextView>(R.id.tvTabTitle)
        currentTextView.setTextColor(Color.WHITE)
    }

    fun setOnTabSelectedListener(listener: OnTabSelectedListener) {
        this.onTabSelectedListener = listener
    }

    fun selectTabAt(index: Int) {
        if (index in 0 until tabs.size) {
            selectTab(index)
        }
    }

    fun getCurrentTab(): Int = selectedTabIndex

    interface OnTabSelectedListener {
        fun onTabSelected(position: Int)
    }

    private data class CustomTab(
        val title: String,
        val view: View
    )
}
