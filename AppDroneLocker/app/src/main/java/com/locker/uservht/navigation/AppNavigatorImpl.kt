package com.locker.uservht.navigation

import android.os.Bundle
import com.locker.uservht.R
import com.delivery.core.navigationComponent.BaseNavigatorImpl
import com.delivery.setting.DemoNavigation
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class AppNavigatorImpl @Inject constructor() : BaseNavigatorImpl(),
    AppNavigation, DemoNavigation {

    override fun openSplashToHomeScreen(bundle: Bundle?) {
        openScreen(R.id.action_splashFragment_to_homeFragment, bundle)
    }

    override fun openSplashToLoginScreen(bundle: Bundle?) {
        openScreen(R.id.action_splashFragment_to_loginFragment, bundle)
    }

    override fun openLoginToHomeScreen(bundle: Bundle?) {
        openScreen(R.id.action_loginFragment_to_homeFragment, bundle)
    }

    override fun openGoogleMap(bundle: Bundle?) {
        openScreen(R.id.action_global_to_deliveryTrackingFragment, bundle)
    }

    override fun openOrderHistory(bundle: Bundle?) {
        openScreen(R.id.action_global_to_orderHistoryFragment, bundle)
    }

    override fun openCreateOrder(bundle: Bundle?) {
     openScreen(R.id.action_global_to_createOrderFragment, bundle)
    }

    override fun openTabAccountToLoginScreen(bundle: Bundle?) {
        openScreen(R.id.action_global_to_loginFragment, bundle)
    }
    override fun openDetailOrder(bundle: Bundle?) {
        openScreen(R.id.action_global_to_deliveryTrackingFragment, bundle)
    }
    
    override fun openHomePage2(bundle: Bundle?) {
        openScreen(R.id.action_global_to_homePage2Fragment, bundle)
    }
    
    override fun openSegmentDetail(bundle: Bundle?) {
        // For now, navigate to delivery tracking fragment
        // You can create a dedicated segment detail fragment later
        openScreen(R.id.action_global_to_deliveryTrackingFragment, bundle)
    }
}