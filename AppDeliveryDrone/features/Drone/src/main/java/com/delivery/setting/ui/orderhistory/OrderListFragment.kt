package com.delivery.setting.ui.orderhistory

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.Constants
import com.delivery.core.utils.toast
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.adapter.OrderHistoryPagingAdapter
import com.delivery.setting.databinding.FragmentOrderListBinding
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderTab
import com.delivery.setting.ui.createorder.OrderProducts
import com.delivery.setting.ui.createorder.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class OrderListFragment :
    BaseFragment<FragmentOrderListBinding, OrderListViewModel>(R.layout.fragment_order_list) {

    @Inject
    lateinit var rxPreferences: RxPreferences
    val map = mutableMapOf<String, List<Product>>()

    private val viewModel: OrderListViewModel by viewModels()
    private val sharedViewModel: SharedOrderHistoryViewModel by activityViewModels()
    private lateinit var orderAdapter: OrderHistoryPagingAdapter

    @Inject
    lateinit var appNavigation: DemoNavigation

    private var orderTab: OrderTab = OrderTab.CURRENT

    override fun getVM(): OrderListViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        map.clear()
        lifecycleScope.launch {
            val gson = Gson()
            val orderProductsListStr = rxPreferences.getOrderProducts().first().toString()
            val type = object : TypeToken<List<OrderProducts>>() {}.type
            val orderProductsList = gson.fromJson<List<OrderProducts>>(orderProductsListStr, type)

            val map = mutableMapOf<String, List<Product>>()
            if (orderProductsList != null) {
                for (op in orderProductsList) {            // orderProducts: List<OrderProduct>
                    map.put(op.orderId, op.products);
                }
            }

            orderTab = arguments?.getSerializable(ARG_ORDER_TAB) as? OrderTab ?: OrderTab.CURRENT
            setupRecyclerView()
            setupObservers()
            viewModel.handleEvent(OrderListEvent.LoadOrders(map.keys.toList(), orderTab))

            startAutoRefresh()
        }
    }

    private fun startAutoRefresh() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.startAutoRefresh(map.keys.toList())
                while (true) {
                    refreshProducts()
                    delay(5_000) // 5 giây
                }
            }
        }
    }

    private suspend fun refreshOrders() {
        viewModel.handleEvent(
            OrderListEvent.LoadOrders(
                orderIds = emptyList(),
                orderTab = orderTab
            )
        )
    }

    private suspend fun refreshProducts() {
        val gson = Gson()
        val orderProductsListStr = rxPreferences.getOrderProducts().first().toString()
        val type = object : TypeToken<List<OrderProducts>>() {}.type
        val orderProductsList = gson.fromJson<List<OrderProducts>>(orderProductsListStr, type)

        val mapProducts = mutableMapOf<String, List<Product>>()
        if (orderProductsList != null) {
            for (op in orderProductsList) {
                mapProducts[op.orderId] = op.products
            }
        }

        orderAdapter.updateProductsMap(mapProducts)
    }

    private fun setupRecyclerView() {
        lifecycleScope.launch {
            val gson = Gson()
            val orderProductsListStr = rxPreferences.getOrderProducts().first().toString()
            val type = object : TypeToken<List<OrderProducts>>() {}.type
            val orderProductsList = gson.fromJson<List<OrderProducts>>(orderProductsListStr, type)

            val mapProducts = mutableMapOf<String, List<Product>>()
            if (orderProductsList != null) {
                for (op in orderProductsList) {            // orderProducts: List<OrderProduct>
                    mapProducts.put(op.orderId, op.products);
                }
            }
            orderAdapter =
                OrderHistoryPagingAdapter(
                    productsMap = mapProducts
                ) { orderItem ->
                    bundleOf().apply {
                        putSerializable(Constants.BundleKeys.ORDER_DETAIL, orderItem)
                    }
                        .also { bundle ->
                            appNavigation.openDeliveryTracking(bundle)
                        }
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
                viewModel.updatedOrders.collect { updatedMap ->
                    orderAdapter.applyOrderUpdates(updatedMap)
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
                        viewModel.handleEvent(OrderListEvent.SearchOrders(map.keys.toList(), query))
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
        viewModel.handleEvent(OrderListEvent.SelectOrder(map.keys.toList(), orderItem.id))
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

        val emptyMessage =
            when (orderTab) {
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
                arguments =
                    Bundle().apply {
                        putSerializable(ARG_ORDER_TAB, orderTab)
                    }
            }
        }
    }
}
