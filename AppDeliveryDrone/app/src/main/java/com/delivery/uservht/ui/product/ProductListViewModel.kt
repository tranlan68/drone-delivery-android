package com.delivery.uservht.ui.product

import androidx.lifecycle.SavedStateHandle
import com.delivery.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject
constructor(
    val savedStateHandle: SavedStateHandle,
) : BaseViewModel()