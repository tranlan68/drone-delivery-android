package com.locker.uservht.ui.home

import androidx.lifecycle.SavedStateHandle
import com.delivery.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle
) : BaseViewModel()
