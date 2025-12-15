package com.delivery.setting.ui.home

import com.delivery.core.base.BaseRepository
import com.delivery.core.network.ApiInterface
import javax.inject.Inject

class HomeRepository
    @Inject
    constructor(
        private val apiInterface: ApiInterface,
    ) : BaseRepository()
