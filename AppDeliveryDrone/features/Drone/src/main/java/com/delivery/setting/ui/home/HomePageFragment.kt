package com.delivery.setting.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.Constants
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.core.utils.toast
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.adapter.OldLocationAdapter
import com.delivery.setting.adapter.ReorderAdapter
import com.delivery.setting.databinding.FragmentHomePageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomePageFragment :
    BaseFragment<FragmentHomePageBinding, HomePageViewModel>(R.layout.fragment_home_page) {
    @Inject
    lateinit var appNavigation: DemoNavigation

    private var reorderAdapter: ReorderAdapter? = null

    private var oldLocationAdapter: OldLocationAdapter? = null

    private val viewModel: HomePageViewModel by viewModels()

    override fun getVM() = viewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupFragmentResultListener()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setupRecyclerViews()
        viewModel.handleEvent(HomePageEvent.LoadDeliveredOrders)
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener(Constants.FragmentResultKeys.SELECTED_LOCKER) { _, bundle ->
            Timber.tag("TrongVQ").d("HomePageFragment   " + "Received locker selection result")
            val lockerId = bundle.getString(Constants.LockerBundleKeys.LOCKER_ID)
            val lockerName = bundle.getString(Constants.LockerBundleKeys.LOCKER_NAME)
            val lockerDescription = bundle.getString(Constants.LockerBundleKeys.LOCKER_DESCRIPTION)
            val latitude = bundle.getDouble(Constants.LockerBundleKeys.LOCKER_LATITUDE)
            val longitude = bundle.getDouble(Constants.LockerBundleKeys.LOCKER_LONGITUDE)
            val locationType = bundle.getString(Constants.LockerBundleKeys.LOCATION_TYPE)

            if (lockerName != null && locationType != null) {
                when (locationType) {
                    Constants.LocationType.PICKUP -> {
                        viewModel.setPickupLocation(lockerName)
                        getString(R.string.success_pickup_location_selected, lockerName).toast(
                            requireContext(),
                        )
                    }

                    Constants.LocationType.DELIVERY -> {
                        viewModel.setDeliveryLocation(lockerName)
                        getString(R.string.success_delivery_location_selected, lockerName).toast(
                            requireContext(),
                        )
                    }
                }
            }
        }
    }

    private fun setupRecyclerViews() {
        reorderAdapter =
            ReorderAdapter { orderItem ->
                bundleOf().apply {
                    putSerializable(Constants.BundleKeys.ORDER_DETAIL, orderItem)
                }
                    .also { bundle ->
                        appNavigation.openDeliveryTracking(bundle)
                    }
            }

        binding.recyclerReorder.apply {
            adapter = reorderAdapter
        }

        oldLocationAdapter =
            OldLocationAdapter { location ->
                viewModel.selectQuickDeliveryLocation(location.address)
                getString(R.string.success_delivery_location_selected, location.address).toast(requireContext())
            }
        binding.rcLocationHistory.apply {
            adapter = oldLocationAdapter
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()

        // Observe UI state for loading, data, and error states
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleViewState(state)
                }
            }
        }

        // Handle UI events
        viewModel.uiEvent.observe(viewLifecycleOwner) { event ->
            handleViewEvent(event)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pickupLocation.collect { location ->
                    if (location != null) {
                        binding.tvPickupLocation.text = location
                        binding.tvPickupLocation.setTextColor(requireContext().getColor(R.color.black))
                    } else {
                        binding.tvPickupLocation.setTextColor(requireContext().getColor(R.color.gray))
                        binding.tvPickupLocation.text =
                            getText(com.delivery.core.R.string.string_pickup_location)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deliveryLocation.collect { location ->
                    if (location != null) {
                        binding.tvDeliveryLocation.text = location
                        binding.tvDeliveryLocation.setTextColor(requireContext().getColor(R.color.black))
                    } else {
                        binding.tvDeliveryLocation.setTextColor(requireContext().getColor(R.color.gray))
                        binding.tvDeliveryLocation.text =
                            getText(com.delivery.core.R.string.string_delivery_location)
                    }
                }
            }
        }
    }

    private fun handleViewState(state: HomePageViewState) {
        // Update loading state
        if (state.isLoading) {
            showLoading()
        } else {
            hideLoading()
        }

        // Update reorder items
        reorderAdapter?.submitList(state.reorderItems)

        // Update old locations
        oldLocationAdapter?.submitList(state.oldLocations)

        // Handle empty states
        if (!state.isLoading && !state.hasError) {
            if (state.reorderItems.isEmpty()) {
                showEmptyReorderData()
            } else {
                hideEmptyReorderData()
            }

            if (state.oldLocations.isEmpty()) {
                showEmptyLocationData()
            } else {
                hideEmptyLocationData()
            }
        }
    }

    private fun handleViewEvent(event: HomePageViewEvent) {
        when (event) {
            is HomePageViewEvent.ShowMessage -> {
                event.message.toast(requireContext())
            }
            is HomePageViewEvent.ShowMessageRes -> {
                val message =
                    if (event.args != null) {
                        getString(event.messageResId, event.args)
                    } else {
                        getString(event.messageResId)
                    }
                message.toast(requireContext())
            }
        }
    }

    private fun showLoading() {
        // Show loading indicators if needed
        binding.progressBar?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        // Hide loading indicators
        binding.progressBar?.visibility = View.GONE
    }

    private fun showEmptyReorderData() {
        // Show empty state for reorder section
        binding.layoutEmptyReorder?.visibility = View.VISIBLE
        binding.recyclerReorder?.visibility = View.GONE
    }

    private fun hideEmptyReorderData() {
        // Hide empty state for reorder section
        binding.layoutEmptyReorder?.visibility = View.GONE
        binding.recyclerReorder?.visibility = View.VISIBLE
    }

    private fun showEmptyLocationData() {
        // Show empty state for location history section
        binding.layoutEmptyLocation?.visibility = View.VISIBLE
        binding.rcLocationHistory?.visibility = View.GONE
    }

    private fun hideEmptyLocationData() {
        // Hide empty state for location history section
        binding.layoutEmptyLocation?.visibility = View.GONE
        binding.rcLocationHistory?.visibility = View.VISIBLE
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.btnCreateOrder.setOnSafeClickListener {
            appNavigation.openCreateOrder()
//            if (viewModel.canCreateOrder()) {
//                appNavigation.openCreateOrder()
//            } else {
//                "Vui lòng chọn điểm lấy hàng và giao hàng".toast(requireContext())
//            }
        }

        binding.tvViewOrderHistory.setOnSafeClickListener {
            appNavigation.openOrderHistory()
        }
        binding.ivArrowDown.setOnSafeClickListener {
            if (viewModel.pickupLocation.value?.isNotEmpty() == true && viewModel.deliveryLocation.value?.isNotEmpty() == true) {
                viewModel.setSwapLocations()
            } else {
                "Vui lòng chọn vị trí".toast(requireContext())
            }
        }

        binding.tvPickupLocation.setOnSafeClickListener {
            val bundle =
                Bundle().apply {
                    // Pass current selected pickup location if exists
                    viewModel.pickupLocation.value?.let { locationName ->
                        putString(Constants.LockerBundleKeys.SELECTED_LOCKER_NAME, locationName)
                    }
                    putString(Constants.LockerBundleKeys.LOCATION_TYPE, Constants.LocationType.PICKUP)
                }
            appNavigation.openSelectLocationLocker(bundle)
        }
        binding.tvDeliveryLocation.setOnSafeClickListener {
            val bundle =
                Bundle().apply {
                    // Pass current selected delivery location if exists
                    viewModel.deliveryLocation.value?.let { locationName ->
                        putString(Constants.LockerBundleKeys.SELECTED_LOCKER_NAME, locationName)
                    }
                    putString(Constants.LockerBundleKeys.LOCATION_TYPE, Constants.LocationType.DELIVERY)
                }
            appNavigation.openSelectLocationLocker(bundle)
        }
    }

    override fun onDestroyView() {
        reorderAdapter = null
        oldLocationAdapter = null
        super.onDestroyView()
    }
}
