package com.delivery.uservht.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.Constants
import com.delivery.uservht.navigation.AppNavigation
import com.delivery.vht.R
import com.delivery.vht.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(R.layout.fragment_home) {
    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: HomeViewModel by viewModels()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        setupBottomNavigationBar()
    }

    /*private fun setupBottomNavigationBar() {
        val navHostFragment =
            childFragmentManager.findFragmentById(
                R.id.nav_host_container,
            ) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }*/

    private fun setupBottomNavigationBar() {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_container) as NavHostFragment

        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        // ======== NHẬN INDEX TRUYỀN VÀO ========
        val index = arguments?.getInt("selected_tab")

        index?.let {
            val targetMenuId = when (it) {
                0 -> R.id.productListFragment
                1 -> R.id.orderHistoryFragment
                2 -> R.id.userInfoFragment
                else -> R.id.productListFragment
            }

            // Chọn đúng tab
            binding.bottomNav.post {
                binding.bottomNav.selectedItemId = targetMenuId
            }
        }
    }


    override fun getVM() = viewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.tag("TrongVQ").d("A   " + "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag("TrongVQ").d("A   " + "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Timber.tag("TrongVQ").d("A   " + "onCreateView")
        setupFragmentResultListener()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag("TrongVQ").d("A   " + "onViewCreated")

    }

    private fun setupFragmentResultListener() {
        Timber.tag("TrongVQ").d("A   " + "setupFragmentResultListener")
        doOnFragmentResult<Bundle>(Constants.FragmentResultKeys.SELECTED_LOCKER) { bundle ->
            val navHostFragment =
                childFragmentManager.findFragmentById(
                    R.id.nav_host_container,
                ) as NavHostFragment
            navHostFragment.childFragmentManager.setFragmentResult(
                Constants.FragmentResultKeys.SELECTED_LOCKER,
                bundle,
            )
        }
    }
}
