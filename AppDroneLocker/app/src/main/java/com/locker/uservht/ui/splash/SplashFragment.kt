package com.locker.uservht.ui.splash

import androidx.fragment.app.viewModels
import com.locker.uservht.R
import com.locker.uservht.databinding.FragmentSplashBinding
import com.locker.uservht.navigation.AppNavigation
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.setTextCompute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment :
    BaseFragment<FragmentSplashBinding, SplashViewModel>(R.layout.fragment_splash) {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: SplashViewModel by viewModels()

    override fun getVM() = viewModel

    override fun bindingAction() {
        super.bindingAction()

        viewModel.actionSPlash.observe(viewLifecycleOwner) {
            when (it) {
                is SplashViewModel.SplashActionState.OpenLogin -> {
                    appNavigation.openSplashToLoginScreen()
                }
                is SplashViewModel.SplashActionState.OpenHome -> {
                    appNavigation.openSplashToHomeScreen()
                }
            }
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.splashTitle.observe(viewLifecycleOwner) {
           /// binding.text.setTextCompute(getString(it))
        }
    }

}