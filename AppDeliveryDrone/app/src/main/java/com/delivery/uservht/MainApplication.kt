package com.delivery.uservht

import com.delivery.core.base.BaseApplication
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication
    @Inject
    constructor() : BaseApplication()
