package com.delivery.setting.ui.userinfo

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.core.utils.toast
import com.delivery.core.utils.toastMessage
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.databinding.FragmentUserInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserInfoFragment : BaseFragment<FragmentUserInfoBinding, UserInfoViewModel>(R.layout.fragment_user_info) {
    private val viewModel: UserInfoViewModel by viewModels()

    @Inject
    lateinit var appNavigation: DemoNavigation

    override fun getVM(): UserInfoViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        observeViewState()
        setUpView()
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.btnSave.setOnSafeClickListener {
//            val name: String = binding.edtName.getTextEditText().trim()
//            val phoneNumber: String = binding.edtPhoneNumber.getTextEditText().trim()
//            viewModel.handleEvent(UserInfoEvent.SaveUserInfo(name, phoneNumber))
            viewModel.logout()
            toastMessage("Đăng xuất thành công")
            appNavigation.openLoginScreen()
        }
    }

    private fun setUpView() {
        binding.edtName.setClearFocus()
        binding.edtPhoneNumber.setClearFocus()

        // Set up name field validation
        binding.edtName.handleEdittextOnTextChange {
            if (binding.edtName.getIsFocus()) {
                if (it.isEmpty()) {
                    binding.edtName.setTextError(getString(R.string.error_name_empty))
                } else {
                    binding.edtName.goneError()
                }
            }
        }

        binding.edtName.handleEdittextOnFocus {
            if (it.isEmpty()) {
                binding.edtName.setTextError(getString(R.string.error_name_empty))
            }
        }

        // Set up phone number field validation
        binding.edtPhoneNumber.handleEdittextOnTextChange {
            if (binding.edtPhoneNumber.getIsFocus()) {
                if (it.isEmpty()) {
                    binding.edtPhoneNumber.setTextError(getString(R.string.error_phone_empty))
                } else {
                    binding.edtPhoneNumber.goneError()
                }
            }
        }

        binding.edtPhoneNumber.handleEdittextOnFocus {
            if (it.isEmpty()) {
                binding.edtPhoneNumber.setTextError(getString(R.string.error_phone_empty))
            }
        }

        binding.edtName.goneError()
        binding.edtPhoneNumber.goneError()
    }

    override fun onPause() {
        super.onPause()
        viewModel.edtNameLiveData.value = binding.edtName.getTextEditText().trim()
        viewModel.edtPhoneLiveData.value = binding.edtPhoneNumber.getTextEditText().trim()
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
        viewModel.edtNameLiveData.observe(viewLifecycleOwner) { name ->
            if (name.isNotEmpty()) {
                binding.edtName.setTextEditText(name)
            }
        }
        viewModel.edtPhoneLiveData.observe(viewLifecycleOwner) { phone ->
            if (phone.isNotEmpty()) {
                binding.edtPhoneNumber.setTextEditText(phone)
            }
        }
    }

    private fun handleViewState(state: UserInfoViewState) {
        binding.apply {
            btnSave.isEnabled = !state.isLoading

            // Set user data if available
            if (state.name.isNotEmpty() && edtName.getTextEditText().isEmpty()) {
                edtName.setTextEditText(state.name)
            }

            if (state.phoneNumber.isNotEmpty() && edtPhoneNumber.getTextEditText().isEmpty()) {
                edtPhoneNumber.setTextEditText(state.phoneNumber)
            }

            // Handle validation errors
            if (state.nameError != null) {
                edtName.setTextError(getString(state.nameError))
            } else {
                edtName.goneError()
            }

            if (state.phoneError != null) {
                edtPhoneNumber.setTextError(getString(state.phoneError))
            } else {
                edtPhoneNumber.goneError()
            }
        }
    }

    private fun handleViewEvent(event: UserInfoViewEvent) {
        when (event) {
            is UserInfoViewEvent.ShowMessage -> {
                event.message.toast(requireContext())
            }
            is UserInfoViewEvent.NavigateBack -> {
                // Navigate back after successful save
                requireActivity().onBackPressed()
            }
        }
    }
}
