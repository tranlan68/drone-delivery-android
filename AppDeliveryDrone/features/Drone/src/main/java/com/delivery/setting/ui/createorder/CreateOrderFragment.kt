package com.delivery.setting.ui.createorder

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.Constants
import com.delivery.core.utils.custom.ToolBarCommon
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.core.utils.toast
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.databinding.FragmentCreateOrderBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CreateOrderFragment :
    BaseFragment<FragmentCreateOrderBinding, CreateOrderViewModel>(R.layout.fragment_create_order) {
    private val viewModel: CreateOrderViewModel by viewModels()

    @Inject
    lateinit var rxPreferences: RxPreferences

    @Inject
    lateinit var appNavigation: DemoNavigation

    private var selectedProducts: List<Product> = emptyList()
    private var fromHubId: String = ""

    // TKL Hardcode
    var locationsPickup = listOf(
        HubItem("693673e2bbb6e622e589b03d", "Hub"),
        HubItem("693673e2bbb6e622e589b03e", "K1"),
        HubItem("693673e2bbb6e622e589b03f", "K2"),
    )

    override fun getVM(): CreateOrderViewModel = viewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        //setupFragmentResultListener()
        fromHubId = ""

        lifecycleScope.launch {
            val productsJson = rxPreferences.getProducts().first()
            val gson = Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<Product>>() {}.type
            selectedProducts = gson.fromJson<List<Product>>(productsJson, type)
            renderSelectedProducts(selectedProducts)
        }
    }

    private fun setupFragmentResultListener() {
        doOnFragmentResult<Bundle>(Constants.FragmentResultKeys.SELECTED_LOCKER) { bundle ->
            Timber.tag("CreateOrderFragment").d("Received locker selection result")
            val lockerId = bundle.getString(Constants.LockerBundleKeys.LOCKER_ID)
            val lockerName = bundle.getString(Constants.LockerBundleKeys.LOCKER_NAME)
            val lockerDescription = bundle.getString(Constants.LockerBundleKeys.LOCKER_DESCRIPTION)
            val latitude = bundle.getDouble(Constants.LockerBundleKeys.LOCKER_LATITUDE)
            val longitude = bundle.getDouble(Constants.LockerBundleKeys.LOCKER_LONGITUDE)
            val locationType = bundle.getString(Constants.LockerBundleKeys.LOCATION_TYPE)

            if (lockerName != null && locationType != null) {
                when (locationType) {
                    Constants.LocationType.PICKUP -> {
                        viewModel.handleEvent(CreateOrderEvent.SelectFromLocation(lockerName, lockerId ?: "ID"))
                        getString(R.string.success_pickup_location_selected, lockerName).toast(
                            requireContext(),
                        )
                    }

                    Constants.LocationType.DELIVERY -> {
                        viewModel.handleEvent(CreateOrderEvent.SelectToLocation(lockerName, lockerId ?: "ID"))
                        getString(R.string.success_delivery_location_selected, lockerName).toast(
                            requireContext(),
                        )
                    }
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        observeViewState()
        setUpView()
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.toolbar.setOnToolBarClickListener(
            object : ToolBarCommon.OnToolBarClickListener() {
                override fun onClickLeft() {
                    appNavigation.navigateUp()
                }

                override fun onClickRight() {
                    // Not used
                }
            },
        )

        // Size selection click listeners
        binding.btnSizeS.setOnSafeClickListener {
            viewModel.handleEvent(CreateOrderEvent.SelectSize(BoxSize.SMALL))
        }

        binding.btnSizeM.setOnSafeClickListener {
            viewModel.handleEvent(CreateOrderEvent.SelectSize(BoxSize.MEDIUM))
        }

        binding.btnSizeL.setOnSafeClickListener {
            viewModel.handleEvent(CreateOrderEvent.SelectSize(BoxSize.LARGE))
        }

        binding.btnSizeXL.setOnSafeClickListener {
            viewModel.handleEvent(CreateOrderEvent.SelectSize(BoxSize.EXTRA_LARGE))
        }

        // Priority selection click listeners
        binding.rbStandard.setOnSafeClickListener {
            viewModel.handleEvent(CreateOrderEvent.SelectPriority(DeliveryPriority.STANDARD))
        }

        binding.rbExpress.setOnSafeClickListener {
            viewModel.handleEvent(CreateOrderEvent.SelectPriority(DeliveryPriority.EXPRESS))
        }

        binding.btnCreateOrder.setOnSafeClickListener {
            if (binding.edtToLocation.getTextEditText() != "") {
                val weight = binding.etWeight.text.toString().trim()
                val receiverPhone = binding.edtReceiverPhone.getTextEditText().trim()
                viewModel.handleEvent(
                    CreateOrderEvent.CreateOrder(
                        weight = weight,
                        receiverPhone = receiverPhone,
                        products = selectedProducts
                    ),
                )
            } else {
                "Vui lòng chọn điểm giao hàng!".toast(requireContext())
            }
        }
    }

    private fun setUpView() {
        // Set default priority to EXPRESS
        binding.rbExpress.isChecked = true

        // Set up validation for receiver phone
        setUpValidation()

        // Clear all errors initially
        binding.edtFromLocation.goneError()
        binding.edtToLocation.goneError()
        binding.edtReceiverPhone.goneError()
        binding.edtReceiverPhone.setInputTypeEdittext(InputType.TYPE_CLASS_PHONE)
        binding.edtReceiverPhone.setDigitsEdittext("0123456789")
        binding.edtReceiverPhone.setNotSpaceAndMaxLengthEdittext(10)
        binding.edtReceiverPhone.setImeOptions(EditorInfo.IME_ACTION_DONE)
        binding.edtToLocation.hideFocusable()
        binding.edtToLocation.setOnClickEditText {
            //openLocationSelector(Constants.LocationType.DELIVERY)

            /*showLocationSelector(locationsDelivery) { selected ->
                //binding.edtToLocation.setTextEditText(selected.name)
                viewModel.handleEvent(CreateOrderEvent.SelectToLocation(selected.name, selected.id ?: "ID"))
            }*/

            /*setupDropdown(
                anchorView = binding.edtToLocation,
                list = locationsDelivery
            ) { selected ->
                viewModel.handleEvent(CreateOrderEvent.SelectToLocation(selected.name, selected.id ?: "ID"))
            }*/


            if (!fromHubId.isEmpty()) {
                // TKL Hardcode
                var locationsDelivery = emptyList<HubItem>()
                if (fromHubId == "693673e2bbb6e622e589b03d") { //HUB
                    locationsDelivery = listOf(
                        HubItem("693673e2bbb6e622e589b03e", "K1"),
                        //HubItem("693673e2bbb6e622e589b03f", "K2"),
                    )
                } else if (fromHubId == "693673e2bbb6e622e589b03e") { // K1
                    locationsDelivery = listOf(
                        HubItem("693673e2bbb6e622e589b03d", "Hub"),
                        //HubItem("693673e2bbb6e622e589b03f", "K2"),
                    )
                } else if (fromHubId == "693673e2bbb6e622e589b03f") { // K2
                    locationsDelivery = listOf(
                        HubItem("693673e2bbb6e622e589b03d", "Hub"),
                        //HubItem("693673e2bbb6e622e589b03e", "K1"),
                    )
                }

                if (!locationsDelivery.isEmpty()) {
                    showDropdown(binding.edtToLocation, locationsDelivery) { selected ->
                        viewModel.handleEvent(
                            CreateOrderEvent.SelectToLocation(
                                selected.name,
                                selected.id ?: "ID"
                            )
                        )
                    }
                } else {
                    "Vui lòng chọn điểm lấy hàng trước!".toast(requireContext())
                }
            } else {
                // show toast
                "Vui lòng chọn điểm lấy hàng trước!".toast(requireContext())
            }
        }
        binding.edtFromLocation.hideFocusable()
        binding.edtFromLocation.setOnClickEditText {
            //openLocationSelector(Constants.LocationType.PICKUP)

            /*showLocationSelector(locationsPickup) { selected ->
                //binding.edtFromLocation.setTextEditText(selected.name)
                viewModel.handleEvent(CreateOrderEvent.SelectFromLocation(selected.name, selected.id ?: "ID"))
            }*/

            /*setupDropdown(
                anchorView = binding.edtFromLocation,
                list = locationsPickup
            ) { selected ->
                viewModel.handleEvent(CreateOrderEvent.SelectFromLocation(selected.name, selected.id ?: "ID"))
            }*/
            showDropdown(binding.edtFromLocation, locationsPickup) { selected ->
                fromHubId = selected.id
                viewModel.handleEvent(CreateOrderEvent.SelectFromLocation(selected.name, selected.id ?: "ID"))
                binding.edtToLocation.setTextEditText("")
                viewModel.handleEvent(
                    CreateOrderEvent.SelectToLocation(
                        "",
                        ""
                    )
                )
            }
        }
    }

    private fun showDropdown(
        anchor: View,
        list: List<HubItem>,
        onSelected: (HubItem) -> Unit
    ) {
        val popup = ListPopupWindow(requireContext())
        popup.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_popup_white)
        )

        val adapter = DropdownAdapter(requireContext(), list)
        popup.setAdapter(adapter)

        popup.anchorView = anchor
        popup.isModal = true
        popup.width = anchor.width
        popup.height = ListPopupWindow.WRAP_CONTENT

        popup.setOnItemClickListener { _, _, position, _ ->
            onSelected(list[position])
            popup.dismiss()
        }

        popup.show()
    }

    private fun setupDropdown(
        anchorView: View,
        list: List<HubItem>,
        onSelected: (HubItem) -> Unit
    ) {
        val popup = ListPopupWindow(requireContext())
        val names = list.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
        popup.setAdapter(adapter)

        popup.anchorView = anchorView
        popup.width = anchorView.width
        popup.isModal = true

        popup.setOnItemClickListener { _, _, position, _ ->
            onSelected(list[position])
            popup.dismiss()
        }

        popup.show()
    }
    private fun showLocationSelector(
        list: List<HubItem>,
        onSelected: (HubItem) -> Unit
    ) {
        val names = list.map { it.name }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Chọn địa điểm")
            .setItems(names) { dialog, index ->
                onSelected(list[index])
                dialog.dismiss()
            }
            .show()
    }

    private fun openLocationSelector(locationType: String) {
        val bundle =
            Bundle().apply {
                // Pass current selected location if exists
                val currentLocation =
                    when (locationType) {
                        Constants.LocationType.PICKUP -> viewModel.uiState.value.fromLocation
                        Constants.LocationType.DELIVERY -> viewModel.uiState.value.toLocation
                        else -> null
                    }
                currentLocation?.let { locationName ->
                    putString(Constants.LockerBundleKeys.SELECTED_LOCKER_NAME, locationName)
                }
                putString(Constants.LockerBundleKeys.LOCATION_TYPE, locationType)
            }
        appNavigation.openSelectLocationLocker(bundle)
    }

    private fun setUpValidation() {
        // Receiver phone validation
        binding.edtReceiverPhone.handleEdittextOnTextChange {
            if (binding.edtReceiverPhone.getIsFocus()) {
                if (it.isEmpty()) {
                    binding.edtReceiverPhone.setTextError(getString(R.string.error_recipient_phone_empty))
                } else if (!isValidPhoneNumber(it)) {
                    binding.edtReceiverPhone.setTextError(getString(R.string.error_recipient_phone_invalid_format))
                } else {
                    binding.edtReceiverPhone.goneError()
                }
            }
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Vietnamese phone number validation
        val phonePattern = "^(\\+84|0)(3[2-9]|5[2689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$"
        return phoneNumber.matches(phonePattern.toRegex())
    }

    private fun observeViewState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleViewState(state)
                    showHideLoading(false)
                }
            }
        }

        // Observe SingleLiveEvent instead of SharedFlow
        viewModel.uiEvent.observe(viewLifecycleOwner) { event ->
            handleViewEvent(event)
            showHideLoading(false)
        }
    }

    private fun handleViewState(state: CreateOrderViewState) {
        binding.apply {
            btnCreateOrder.isEnabled = !state.isLoading

            // Update size selection UI
            updateSizeSelection(state.selectedSize)

            // Update priority selection UI
            updatePrioritySelection(state.selectedPriority)

            // Update weight display
            etWeight.setText(state.weight)

            // Update location displays
            if (state.fromLocation != null) {
                edtFromLocation.setTextEditText(state.fromLocation)
                edtFromLocation.goneError()
            } else {
                edtFromLocation.setTextEditText("")
            }

            if (state.toLocation != null) {
                edtToLocation.setTextEditText(state.toLocation)
                edtToLocation.goneError()
            } else {
                edtToLocation.setTextEditText("")
            }

            // Handle validation errors
            if (state.fromLocationError != null) {
                edtFromLocation.setTextError(getString(state.fromLocationError))
            } else {
                edtFromLocation.goneError()
            }

            if (state.toLocationError != null) {
                edtToLocation.setTextError(getString(state.toLocationError))
            } else {
                edtToLocation.goneError()
            }

            if (state.receiverPhoneError != null) {
                edtReceiverPhone.setTextError(getString(state.receiverPhoneError))
            } else {
                edtReceiverPhone.goneError()
            }
        }
    }

    private fun updateSizeSelection(selectedSize: BoxSize) {
        binding.apply {
            // Reset all sizes to default
            btnSizeS.setBackgroundResource(R.drawable.bg_size_unselected)
            btnSizeM.setBackgroundResource(R.drawable.bg_size_unselected)
            btnSizeL.setBackgroundResource(R.drawable.bg_size_unselected)
            btnSizeXL.setBackgroundResource(R.drawable.bg_size_unselected)
            btnSizeS.setTextColor(resources.getColor(R.color.color_f11223, null))
            btnSizeM.setTextColor(resources.getColor(R.color.color_f11223, null))
            btnSizeL.setTextColor(resources.getColor(R.color.color_f11223, null))
            btnSizeXL.setTextColor(resources.getColor(R.color.color_f11223, null))

            // Set selected size and update package illustration
            when (selectedSize) {
                BoxSize.SMALL -> {
                    btnSizeS.setBackgroundResource(R.drawable.bg_size_selected)
                    ivPackageIllustration.setImageResource(com.delivery.core.R.drawable.ic_box_size_s)
                    btnSizeS.setTextColor(resources.getColor(R.color.white, null))
                }

                BoxSize.MEDIUM -> {
                    btnSizeM.setBackgroundResource(R.drawable.bg_size_selected)
                    ivPackageIllustration.setImageResource(com.delivery.core.R.drawable.ic_box_size_m)
                    btnSizeM.setTextColor(resources.getColor(R.color.white, null))
                }

                BoxSize.LARGE -> {
                    btnSizeL.setBackgroundResource(R.drawable.bg_size_selected)
                    ivPackageIllustration.setImageResource(com.delivery.core.R.drawable.ic_box_size_l)
                    btnSizeL.setTextColor(resources.getColor(R.color.white, null))
                }

                BoxSize.EXTRA_LARGE -> {
                    btnSizeXL.setBackgroundResource(R.drawable.bg_size_selected)
                    ivPackageIllustration.setImageResource(com.delivery.core.R.drawable.ic_box_size_xl)
                    btnSizeXL.setTextColor(resources.getColor(R.color.white, null))
                }
            }
        }
    }

    private fun updatePrioritySelection(selectedPriority: DeliveryPriority) {
        binding.apply {
            when (selectedPriority) {
                DeliveryPriority.STANDARD -> rbStandard.isChecked = true
                DeliveryPriority.EXPRESS -> rbExpress.isChecked = true
            }
        }
    }

    private fun handleViewEvent(event: CreateOrderViewEvent) {
        when (event) {
            is CreateOrderViewEvent.ShowMessage -> {
                event.message.toast(requireContext())
            }

            is CreateOrderViewEvent.ShowMessageRes -> {
                getString(event.messageResId).toast(requireContext())
            }

            is CreateOrderViewEvent.NavigateBack -> {
                appNavigation.navigateUp()
            }

            is CreateOrderViewEvent.NavigateToOrderSuccess -> {
                val message =
                    if (event.response != null) {
                        getString(
                            com.delivery.core.R.string.order_create_success_with_id,
                            event.response.orderId,
                        )
                    } else {
                        getString(com.delivery.core.R.string.order_create_success)
                    }
                message.toast(requireContext())
                //appNavigation.navigateUp()
                val bundle = bundleOf("selected_tab" to 1)
                appNavigation.openHomeScreeAfterCreatedOrder(bundle)
//                val bundle = bundleOf("selected_tab" to 1)  // ví dụ
//                appNavigation.navigateUp(R.id.action_global_to_homeFragment, bundle)
                /*appNavigation.navigateUp(
                    R.id.action_global_to_homeFragment,
                    bundleOf("selectedTab" to 2)
                )

                findNavController().navigate(R.id.action_global_to_homeFragment, bundle)
                findNavController().navigate(
                    com.delivery.vht.R.id.action_global_to_homeFragment
                )*/
            }
        }
    }

    /**
     * HIỂN THỊ DANH SÁCH SẢN PHẨM ĐÃ CHỌN
     * -----------------------------------
     * Icon + Tên theo dạng dọc
     */
    private fun renderSelectedProducts(products: List<Product>) {
        val grid = binding.glSelectedProducts
        val title = binding.tvSelectedProductsTitle
        val card = binding.cardSelectedProducts

        grid.removeAllViews()

        if (products.isEmpty()) {
            title.visibility = View.GONE
            card.visibility = View.GONE
            return
        }

        title.visibility = View.VISIBLE
        card.visibility = View.VISIBLE

        val inflater = LayoutInflater.from(requireContext())

        products.forEach { product ->
            val item = inflater.inflate(R.layout.item_selected_product_vertical, grid, false)

            item.findViewById<ImageView>(R.id.ivIcon).setImageResource(product.iconRes)
            item.findViewById<TextView>(R.id.tvName).text = product.name

            grid.addView(item)
        }
    }

}
