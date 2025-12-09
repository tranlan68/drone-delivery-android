package com.delivery.setting.ui.orderhistory

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.toast
import com.delivery.setting.R
import com.delivery.setting.adapter.OrderHistoryPagingAdapter
import com.delivery.setting.databinding.FragmentOrderListBinding
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderTab
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class OrderListFragment : BaseFragment<FragmentOrderListBinding, OrderListViewModel>(R.layout.fragment_order_list) {

    private val viewModel: OrderListViewModel by viewModels()
    private val sharedViewModel: SharedOrderHistoryViewModel by activityViewModels()
    private lateinit var orderAdapter: OrderHistoryPagingAdapter
    
    private var orderTab: OrderTab = OrderTab.CURRENT

    override fun getVM(): OrderListViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        
        orderTab = arguments?.getSerializable(ARG_ORDER_TAB) as? OrderTab ?: OrderTab.CURRENT
        
        setupRecyclerView()
        setupObservers()
        
        viewModel.handleEvent(OrderListEvent.LoadOrders(orderTab))
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderHistoryPagingAdapter { orderItem ->
            handleOrderItemClick(orderItem)
        }
        
        binding.recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
        
        // Handle loading states
        orderAdapter.addLoadStateListener { loadState ->
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    showLoading()
                }
                is LoadState.NotLoading -> {
                    hideLoading()
                    if (orderAdapter.itemCount == 0) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                    }
                }
                is LoadState.Error -> {
                    hideLoading()
                    val error = (loadState.refresh as LoadState.Error).error
                    "Có lỗi xảy ra: ${error.message}".toast(requireContext())
                }
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleViewState(state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ordersPagingData.collectLatest { pagingData ->
                    orderAdapter.submitData(pagingData)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    handleViewEvent(event)
                }
            }
        }

        // Observe shared search query
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.searchQuery.collect { query ->
                    if (isAdded && view != null) {
                        Timber.d("Search query updated: $query")
                        viewModel.handleEvent(OrderListEvent.SearchOrders(query))
                    }
                }
            }
        }
    }

    private fun handleViewState(state: OrderListViewState) {
        // Handle any additional view state updates here
    }

    private fun handleViewEvent(event: OrderListViewEvent) {
        when (event) {
            is OrderListViewEvent.ShowMessage -> {
                event.message.toast(requireContext())
            }
            is OrderListViewEvent.ShowOrderDetail -> {
                // Navigate to order detail screen
                "Xem chi tiết đơn hàng: ${event.orderId}".toast(requireContext())
            }
        }
    }

    private fun handleOrderItemClick(orderItem: OrderHistoryItem) {
        viewModel.handleEvent(OrderListEvent.SelectOrder(orderItem.id))
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewOrders.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerViewOrders.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerViewOrders.visibility = View.GONE
        
        val emptyMessage = when (orderTab) {
            OrderTab.CURRENT -> "Không có đơn hàng hiện tại"
            OrderTab.HISTORY -> "Không có lịch sử đơn hàng"
        }
        binding.tvEmptyMessage.text = emptyMessage
    }

    private fun hideEmptyState() {
        binding.layoutEmptyState.visibility = View.GONE
    }

    // No need for updateSearchQuery anymore since we use SharedViewModel

    companion object {
        private const val ARG_ORDER_TAB = "order_tab"

        fun newInstance(orderTab: OrderTab): OrderListFragment {
            return OrderListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ORDER_TAB, orderTab)
                }
            }
        }
    }
}
