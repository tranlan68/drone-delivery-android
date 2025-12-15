package com.delivery.uservht.navigation

import android.os.Bundle
import com.delivery.core.navigationComponent.BaseNavigatorImpl
import com.delivery.setting.DemoNavigation
import com.delivery.vht.R
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class AppNavigatorImpl
    @Inject
    constructor() :
    BaseNavigatorImpl(),
        AppNavigation,
        DemoNavigation {
        override fun openSplashToHomeScreen(bundle: Bundle?) {
            openScreen(R.id.action_global_to_chooseServiceFragment, bundle)
        }

        override fun openSplashToLoginScreen(bundle: Bundle?) {
            openScreen(R.id.action_splashFragment_to_loginFragment, bundle)
        }

        override fun openLoginToHomeScreen(bundle: Bundle?) {
            openScreen(R.id.action_loginFragment_to_homeFragment, bundle)
        }

        override fun openMainScreen(bundle: Bundle?) {
            //openScreen(R.id.action_global_to_productListFragment, bundle)
            //openScreen(R.id.action_global_to_homeFragment, bundle)
            openScreen(R.id.action_global_to_chooseServiceFragment, bundle)
        }

        override fun openHomeScreen(bundle: Bundle?) {
            openScreen(R.id.action_global_to_homeFragment, bundle)
        }

        override fun openHomeScreeAfterCreatedOrder(bundle: Bundle?) {
            openScreen(R.id.action_global_to_homeFragment, bundle)
        }

        override fun openSelectLocationLocker(bundle: Bundle?) {
            openScreen(R.id.action_global_to_selectLocationLockerFragment, bundle)
        }

        override fun openOrderHistory(bundle: Bundle?) {
            openScreen(R.id.action_global_to_orderHistoryFragment, bundle)
        }

        override fun openCreateOrder(bundle: Bundle?) {
            openScreen(R.id.action_global_to_createOrderFragment, bundle)
        }

        override fun openDetailOrder(bundle: Bundle?) {
            openScreen(R.id.action_global_to_deliveryTrackingFragment, bundle)
        }

        override fun openDeliveryTracking(bundle: Bundle?) {
            openScreen(R.id.action_global_to_deliveryTrackingFragment, bundle)
        }

        override fun openLoginScreen(bundle: Bundle?) {
            openScreen(R.id.action_global_to_loginFragment, bundle)
        }
    }
