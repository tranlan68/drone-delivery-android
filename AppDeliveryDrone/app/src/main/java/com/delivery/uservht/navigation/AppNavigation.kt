package com.delivery.uservht.navigation

import android.os.Bundle
import com.delivery.core.navigationComponent.BaseNavigator

interface AppNavigation : BaseNavigator {
    fun openSplashToHomeScreen(bundle: Bundle? = null)

    fun openSplashToLoginScreen(bundle: Bundle? = null)

    fun openLoginToHomeScreen(bundle: Bundle? = null)

    fun openMainScreen(bundle: Bundle? = null)

    fun openHomeScreen(bundle: Bundle? = null)
}
