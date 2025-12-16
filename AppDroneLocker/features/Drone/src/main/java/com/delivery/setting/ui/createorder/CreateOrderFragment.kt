package com.delivery.setting.ui.createorder

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.custom.ToolBarCommon
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.core.utils.toast
import com.delivery.setting.R
import com.delivery.setting.databinding.FragmentCreateOrderBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateOrderFragment : BaseFragment<FragmentCreateOrderBinding, CreateOrderViewModel>(R.layout.fragment_create_order) {

    private val viewModel: CreateOrderViewModel by viewModels()


    override fun getVM(): CreateOrderViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        observeViewState()
        setUpView()
    }

    override fun setOnClick() {
        super.setOnClick()
        
        binding.toolbar.setOnToolBarClickListener(object : ToolBarCommon.OnToolBarClickListener() {
            override fun onClickLeft() {
                requireActivity().onBackPressed()
            }

            override fun onClickRight() {
                // Not used
            }
        })
        
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
        
        binding.btnCreateOrder.setOnSafeClickListener {
            val fromLocation = binding.edtFromLocation.getTextEditText().trim()
            val toLocation = binding.edtToLocation.getTextEditText().trim()
            val recipientName = binding.edtRecipientName.getTextEditText().trim()
            val recipientPhone = binding.edtRecipientPhone.getTextEditText().trim()
            val packageType = binding.edtPackageType.getTextEditText().trim()
            val weight = binding.etWeight.text.toString().trim()
            
            viewModel.handleEvent(
                CreateOrderEvent.CreateOrder(
                    fromLocation = fromLocation,
                    toLocation = toLocation,
                    recipientName = recipientName,
                    recipientPhone = recipientPhone,
                    packageType = packageType,
                    weight = weight
                )
            )
        }
    }

    private fun setUpView() {
        binding.edtFromLocation.setClearFocus()
        binding.edtToLocation.setClearFocus()
        binding.edtRecipientName.setClearFocus()
        binding.edtRecipientPhone.setClearFocus()
        binding.edtPackageType.setClearFocus()
        
        // Set up validation for input fields
        setUpValidation()
        
        // Clear all errors initially
        binding.edtFromLocation.goneError()
        binding.edtToLocation.goneError()
        binding.edtRecipientName.goneError()
        binding.edtRecipientPhone.goneError()
        binding.edtPackageType.goneError()
    }
    
    private fun setUpValidation() {
        // From location validation
        binding.edtFromLocation.handleEdittextOnTextChange {
            if (binding.edtFromLocation.getIsFocus()) {
                if (it.isEmpty()) {
                    binding.edtFromLocation.setTextError(getString(R.string.error_from_location_empty))
                } else {
                    binding.edtFromLocation.goneError()
                }
            }
        }
        
        // To location validation
        binding.edtToLocation.handleEdittextOnTextChange {
            if (binding.edtToLocation.getIsFocus()) {
                if (it.isEmpty()) {
                    binding.edtToLocation.setTextError(getString(R.string.error_to_location_empty))
                } else {
                    binding.edtToLocation.goneError()
                }
            }
        }
        
        // Recipient name validation
        binding.edtRecipientName.handleEdittextOnTextChange {
            if (binding.edtRecipientName.getIsFocus()) {
                if (it.isEmpty()) {
                    binding.edtRecipientName.setTextError(getString(R.string.error_recipient_name_empty))
                } else {
                    binding.edtRecipientName.goneError()
                }
            }
        }
        
        // Recipient phone validation
        binding.edtRecipientPhone.handleEdittextOnTextChange {
            if (binding.edtRecipientPhone.getIsFocus()) {
                if (it.isEmpty()) {
                    binding.edtRecipientPhone.setTextError(getString(R.string.error_recipient_phone_empty))
                } else {
                    binding.edtRecipientPhone.goneError()
                }
            }
        }
        
        // Package type validation
        binding.edtPackageType.handleEdittextOnTextChange {
            if (binding.edtPackageType.getIsFocus()) {
                if (it.isEmpty()) {
                    binding.edtPackageType.setTextError(getString(R.string.error_package_type_empty))
                } else {
                    binding.edtPackageType.goneError()
                }
            }
        }
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    handleViewEvent(event)
                    showHideLoading(false)
                }
            }
        }
    }

    private fun handleViewState(state: CreateOrderViewState) {
        binding.apply {
            btnCreateOrder.isEnabled = !state.isLoading
            
            // Update size selection UI
            updateSizeSelection(state.selectedSize)
            
            // Update weight display
            etWeight.setText(state.weight)
            
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
            
            if (state.recipientNameError != null) {
                edtRecipientName.setTextError(getString(state.recipientNameError))
            } else {
                edtRecipientName.goneError()
            }
            
            if (state.recipientPhoneError != null) {
                edtRecipientPhone.setTextError(getString(state.recipientPhoneError))
            } else {
                edtRecipientPhone.goneError()
            }
            
            if (state.packageTypeError != null) {
                edtPackageType.setTextError(getString(state.packageTypeError))
            } else {
                edtPackageType.goneError()
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
            
            // Set selected size and update package illustration
            when (selectedSize) {
                BoxSize.SMALL -> {
                    btnSizeS.setBackgroundResource(R.drawable.bg_size_selected)
                    ivPackageIllustration.setImageResource(com.delivery.core.R.drawable.ic_box_size_s)
                }
                BoxSize.MEDIUM -> {
                    btnSizeM.setBackgroundResource(R.drawable.bg_size_selected)
                    ivPackageIllustration.setImageResource(com.delivery.core.R.drawable.ic_box_size_m)
                }
                BoxSize.LARGE -> {
                    btnSizeL.setBackgroundResource(R.drawable.bg_size_selected)
                    ivPackageIllustration.setImageResource(com.delivery.core.R.drawable.ic_box_size_l)
                }
                BoxSize.EXTRA_LARGE -> {
                    btnSizeXL.setBackgroundResource(R.drawable.bg_size_selected)
                    ivPackageIllustration.setImageResource(com.delivery.core.R.drawable.ic_box_size_xl)
                }
            }
        }
    }

    private fun handleViewEvent(event: CreateOrderViewEvent) {
        when (event) {
            is CreateOrderViewEvent.ShowMessage -> {
                event.message.toast(requireContext())
            }
            is CreateOrderViewEvent.NavigateBack -> {
                requireActivity().onBackPressed()
            }
            is CreateOrderViewEvent.NavigateToOrderSuccess -> {
                "Đơn hàng đã được tạo thành công!".toast(requireContext())
                requireActivity().onBackPressed()
            }
        }
    }
}
