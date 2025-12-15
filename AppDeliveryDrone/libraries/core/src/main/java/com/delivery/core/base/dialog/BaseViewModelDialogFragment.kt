package com.delivery.core.base.dialog

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.delivery.core.base.BaseViewModel

abstract class BaseViewModelDialogFragment<BD : ViewDataBinding, VM : BaseViewModel>(
    @LayoutRes id: Int,
) :
    BaseViewBindingDialogFragment<BD>(id) {
    private lateinit var viewModel: VM

    abstract fun getVM(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getVM()
    }
}
