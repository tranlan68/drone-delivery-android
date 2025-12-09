package com.locker.uservht.ui.auth

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.core.utils.toast
import com.locker.uservht.navigation.AppNavigation
import com.locker.uservht.R
import com.locker.uservht.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern
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
        viewModel.handleEvent(LoginEvent.LoadLockers)
    }

    override fun setOnClick() {
        super.setOnClick()
        
        binding.btnLogin.setOnSafeClickListener {
            viewModel.handleEvent(LoginEvent.PerformLogin)
        }


    }

    private fun setUpView(){
        val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        binding.lockerDropdown.setAdapter(adapter)
        binding.lockerDropdown.setOnItemClickListener { _, _, position, _ ->
            val uiState: LoginViewState = viewModel.uiState.value
            val lockers = uiState.lockers
            if (position in lockers.indices) {
                viewModel.handleEvent(LoginEvent.SelectLocker(lockers[position]))
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

    private fun handleViewState(state: LoginViewState) {
        binding.apply {
            btnLogin.isEnabled = !state.isLoading
            val adapter = binding.lockerDropdown.adapter as? ArrayAdapter<String>
            val names: List<String> = state.lockers.map { it.lockerName }
            if (adapter != null) {
                adapter.clear()
                adapter.addAll(names)
                adapter.notifyDataSetChanged()
            }
            if (state.lockerError != null) {
                binding.lockerInputLayout.error = getString(state.lockerError)
            } else {
                binding.lockerInputLayout.error = null
            }
        }
    }

    private fun handleViewEvent(event: LoginViewEvent) {
        when (event) {
            is LoginViewEvent.ShowMessage -> {
                event.message.toast(requireContext())
            }
            is LoginViewEvent.NavigateToHome -> {
                // Navigate to main screen after successful login
                appNavigation.openLoginToHomeScreen()
            }
        }
    }
}