package com.delivery.setting.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.Constants
import com.delivery.core.utils.toast
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.adapter.OrderAdapter
import com.delivery.setting.databinding.FragmentHomePageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomePageFragment :
    BaseFragment<FragmentHomePageBinding, HomePageViewModel>(R.layout.fragment_home_page) {

    @Inject
    lateinit var appNavigation: DemoNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private var orderAdapter: OrderAdapter? = null
    private var currentLockerId: String = ""

    private val viewModel: HomePageViewModel by viewModels()

    override fun getVM(): HomePageViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.viewModel = viewModel
        initializeCurrentLockerId()
        setupRecyclerView()
        viewModel.handleEvent(HomePageEvent.LoadData)
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            onOrderClick = { order ->
                handleOrderClick(order)
            },
            onActionClick = { order, displayStyle ->
                handleOrderActionClick(order, displayStyle)
            },
            currentLockerId = currentLockerId
        )

        binding.recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun setupObservers() {
        observeUiState()
        observeOrders()
        observeLoadingState()
        observeUiEvents()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleViewState(state)
                }
            }
        }
    }

    private fun observeOrders() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orders.collect { orders ->
                    Timber.d("Collected ${orders.size} orders")
                    updateOrdersList(orders)
                }
            }
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateLoadingState(state.isLoading)
                }
            }
        }
    }

    private fun observeUiEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    handleViewEvent(event)
                }
            }
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        setupObservers()
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.btnViewSegments.setOnClickListener {
            appNavigation.openHomePage2()
        }
    }

    private fun handleViewState(state: HomePageViewState) {
        handleErrorState(state.error)
        updateOrdersTitle(state.pendingOrdersCount)
    }

    private fun handleErrorState(error: String?) {
        if (error != null) {
            error.toast(requireContext())
        }
    }

    private fun handleViewEvent(event: HomePageViewEvent) {
        when (event) {
            is HomePageViewEvent.ShowMessage -> {
                showToastMessage(event.message)
            }
            is HomePageViewEvent.ShowMessageRes -> {
                showToastMessageRes(event.messageResId, event.args)
            }
            is HomePageViewEvent.NavigateToOrderDetail -> {
                navigateToOrderDetail(event.orderId)
            }
        }
    }

    private fun showToastMessage(message: String) {
        message.toast(requireContext())
    }

    private fun showToastMessageRes(messageResId: Int, args: Any?) {
        val message = if (args != null) {
            getString(messageResId, args)
        } else {
            getString(messageResId)
        }
        message.toast(requireContext())
    }

    private fun navigateToOrderDetail(orderId: String) {
        "Xem chi tiết đơn hàng: $orderId".toast(requireContext())
    }


    private fun updateOrdersList(orders: List<com.delivery.setting.model.Order>) {
        orderAdapter?.updateOrders(orders)
        updateEmptyStateVisibility(orders.isEmpty())
    }

    private fun updateEmptyStateVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            showOrdersEmptyState()
        } else {
            hideOrdersEmptyState()
        }
    }

    private fun showOrdersEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerViewOrders.visibility = View.GONE
    }

    private fun hideOrdersEmptyState() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.recyclerViewOrders.visibility = View.VISIBLE
    }

    private fun updateOrdersTitle(pendingCount: Int) {
        val title = getString(com.delivery.core.R.string.string_pending_orders_title, pendingCount)
        binding.tvOrdersTitle.text = title
    }

    private fun initializeCurrentLockerId() {
        lifecycleScope.launch {
            try {
                currentLockerId = rxPreferences.getSelectedLockerId().first() ?: ""
            } catch (e: Exception) {
                currentLockerId = ""
                Timber.e(e, "Failed to load current locker ID")
            }
        }
    }

    private fun handleOrderClick(order: com.delivery.setting.model.Order) {
        val bundle = bundleOf().apply {
            putSerializable(Constants.BundleKeys.ORDER_DETAIL, order)
        }
        appNavigation.openDetailOrder(bundle)
    }

    private fun handleOrderActionClick(order: com.delivery.setting.model.Order, displayStyle: com.delivery.setting.model.OrderDisplayStyle) {
        viewModel.handleEvent(HomePageEvent.OrderAction(order, displayStyle))
    }

    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) {
            showHideLoading(true)
        } else {
            showHideLoading(false)
        }
    }


    override fun onDestroyView() {
        orderAdapter = null
        super.onDestroyView()
    }
}