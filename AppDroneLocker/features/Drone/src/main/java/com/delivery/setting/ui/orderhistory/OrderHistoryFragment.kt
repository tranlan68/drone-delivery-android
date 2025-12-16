package com.delivery.setting.ui.orderhistory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.custom.ToolBarCommon
import com.delivery.setting.R
import com.delivery.setting.adapter.OrderHistoryTabAdapter
import com.delivery.setting.databinding.FragmentOrderHistoryBinding
import com.delivery.setting.model.OrderTab

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderHistoryFragment : BaseFragment<FragmentOrderHistoryBinding, OrderHistoryViewModel>(R.layout.fragment_order_history) {

    private val viewModel: OrderHistoryViewModel by viewModels()
    private val sharedViewModel: SharedOrderHistoryViewModel by activityViewModels()
    private lateinit var tabAdapter: OrderHistoryTabAdapter
    private val fragments = mutableListOf<OrderListFragment>()

    override fun getVM(): OrderHistoryViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        
        setupTabLayout()
        setupSearchView()
        observeViewState()
    }


    private fun setupTabLayout() {
        // Create fragments for each tab
        fragments.clear()
        fragments.add(OrderListFragment.newInstance(OrderTab.CURRENT))
        fragments.add(OrderListFragment.newInstance(OrderTab.HISTORY))

        tabAdapter = OrderHistoryTabAdapter(
            fragments = fragments as List<Fragment>,
            fragmentManager = childFragmentManager,
            lifecycle = viewLifecycleOwner.lifecycle
        )

        binding.viewPager.adapter = tabAdapter
        
        // Setup custom tabs
        binding.customTabLayout.addTab(OrderTab.CURRENT.tabName)
        binding.customTabLayout.addTab(OrderTab.HISTORY.tabName)
        
        // Connect CustomTabLayout with ViewPager2
        binding.customTabLayout.setOnTabSelectedListener(object : CustomTabLayout.OnTabSelectedListener {
            override fun onTabSelected(position: Int) {
                binding.viewPager.currentItem = position
                viewModel.handleEvent(OrderHistoryEvent.SwitchTab(position))
            }
        })
        
        // Listen to ViewPager2 page changes to update tab selection
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.customTabLayout.selectTabAt(position)
            }
        })
        binding.customTabLayout.selectTabAt(1)
    }

    private fun setupSearchView() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                sharedViewModel.updateSearchQuery(query)
                viewModel.handleEvent(OrderHistoryEvent.SearchOrders(query))
            }
        })

        binding.btnClearSearch.setOnClickListener {
            binding.edtSearch.text?.clear()
        }
    }

    private fun observeViewState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleViewState(state)
                }
            }
        }

        // No need to observe uiEvent anymore since we use SharedViewModel
    }

    private fun handleViewState(state: OrderHistoryViewState) {
        // Handle view state updates
        binding.btnClearSearch.visibility = if (state.searchQuery.isNotEmpty()) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }

    // No need for handleViewEvent anymore since we use SharedViewModel

    private fun getCurrentVisibleFragment(): OrderListFragment? {
        val currentPosition = binding.viewPager.currentItem
        return if (currentPosition < fragments.size) {
            fragments[currentPosition]
        } else null
    }

    override fun setOnClick() {
        super.setOnClick()
        // Additional click listeners if needed
    }
}
