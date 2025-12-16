package com.delivery.setting

import android.os.Bundle
import com.delivery.core.navigationComponent.BaseNavigator

interface DemoNavigation : BaseNavigator {


    fun openGoogleMap(bundle: Bundle? = null)
    
    fun openOrderHistory(bundle: Bundle? = null)
    
    fun openCreateOrder(bundle: Bundle? = null)

    fun openTabAccountToLoginScreen(bundle: Bundle? = null)

    fun openDetailOrder(bundle: Bundle? = null)
    
    fun openHomePage2(bundle: Bundle? = null)
    
    fun openSegmentDetail(bundle: Bundle? = null)
}