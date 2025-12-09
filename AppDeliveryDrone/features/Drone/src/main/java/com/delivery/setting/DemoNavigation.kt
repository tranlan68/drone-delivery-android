package com.delivery.setting

import android.os.Bundle
import com.delivery.core.navigationComponent.BaseNavigator

interface DemoNavigation : BaseNavigator {
    fun openSelectLocationLocker(bundle: Bundle? = null)

    fun openOrderHistory(bundle: Bundle? = null)

    fun openCreateOrder(bundle: Bundle? = null)

    fun openDetailOrder(bundle: Bundle? = null)

    fun openDeliveryTracking(bundle: Bundle? = null)

    fun openLoginScreen(bundle: Bundle? = null)

    fun openHomeScreeAfterCreatedOrder(bundle: Bundle? = null)
}
