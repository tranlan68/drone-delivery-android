package com.delivery.uservht.ui.auth

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.core.utils.toast
import com.delivery.uservht.navigation.AppNavigation
import com.delivery.vht.R
import com.delivery.vht.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>(R.layout.fragment_login) {
    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: LoginViewModel by viewModels()

    override fun getVM(): LoginViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        observeViewState()
        setUpView()
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.btnLogin.setOnSafeClickListener {
            val username: String = binding.edtPhoneNumber.getTextEditText().toString().trim()
            val password: String = binding.edtPassword.getTextEditText().toString().trim()
//            viewModel.handleEvent(LoginEvent.PerformLogin(username, password))
            viewModel.setUserToken(username)
            appNavigation.openLoginToHomeScreen()
        }
    }

    private fun setUpView() {
        binding.edtPhoneNumber.setClearFocus()
        binding.edtPassword.setNotSpaceAndMaxLengthEdittext(16)
        binding.edtPassword.handleEdittextOnTextChange {
            binding.edtPassword.goneError()
        }
        binding.edtPhoneNumber.handleEdittextOnFocus {
            if (it.isEmpty()) {
                binding.edtPhoneNumber.setTextError(getString(R.string.hint_username_or_phone))
            }
        }
        binding.edtPhoneNumber.handleEdittextOnTextChange {
            if (!binding.edtPhoneNumber.getIsFocus()) return@handleEdittextOnTextChange
            if (it.isEmpty()) {
                binding.edtPhoneNumber.setTextError(getString(com.delivery.core.R.string.notify_enter_user_name))
            } else {
                binding.edtPhoneNumber.goneError()
            }
        }
        binding.edtPhoneNumber.goneError()
    }

    private fun observeViewState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleViewState(state)
                    showHideLoading(state.isLoading)
                }
            }
        }

        // Observe SingleLiveEvent instead of SharedFlow
        viewModel.uiEvent.observe(viewLifecycleOwner) { event ->
            handleViewEvent(event)
            showHideLoading(false)
        }
    }

    private fun handleViewState(state: LoginViewState) {
        binding.apply {
            btnLogin.isEnabled = !state.isLoading

            if (state.usernameError != null) {
                binding.edtPhoneNumber.setTextError(getString(state.usernameError))
            } else {
                binding.edtPhoneNumber.goneError()
            }

            if (state.passwordError != null) {
                binding.edtPassword.setTextError(getString(state.passwordError))
            } else {
                binding.edtPassword.goneError()
            }
        }
    }

    private fun handleViewEvent(event: LoginViewEvent) {
        when (event) {
            is LoginViewEvent.ShowMessage -> {
                event.message.toast(requireContext())
            }
            is LoginViewEvent.ShowMessageRes -> {
                getString(event.messageResId).toast(requireContext())
            }
            is LoginViewEvent.NavigateToHome -> {
                // Navigate to main screen after successful login
                appNavigation.openLoginToHomeScreen()
            }
            is LoginViewEvent.NavigateToForgotPassword -> {
                // Navigate to forgot password screen
                getString(com.delivery.core.R.string.navigate_to_forgot_password).toast(requireContext())
            }
            is LoginViewEvent.NavigateToRegister -> {
                // Navigate to register screen
                getString(com.delivery.core.R.string.navigate_to_register).toast(requireContext())
            }
        }
    }
}
