package com.delivery.uservht.ui.splash

import androidx.fragment.app.viewModels
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.uservht.navigation.AppNavigation
import com.delivery.vht.R
import com.delivery.vht.databinding.FragmentSplashBinding
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
                is SplashViewModel.SplashActionState.Finish -> appNavigation.openMainScreen()
                is SplashViewModel.SplashActionState.LoginAccount -> appNavigation.openSplashToLoginScreen()
            }
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
    }
}
