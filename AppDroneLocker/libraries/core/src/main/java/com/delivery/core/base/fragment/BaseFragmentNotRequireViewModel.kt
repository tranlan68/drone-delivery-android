package com.delivery.core.base.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.delivery.core.base.activity.BaseActivityNotRequireViewModel

abstract class BaseFragmentNotRequireViewModel<BD : ViewDataBinding>(@LayoutRes id: Int) :
    Fragment(id) {

    private var _binding: BD? = null
    protected val binding: BD
        get() = _binding
            ?: throw IllegalStateException("Cannot access view after view destroyed or before view creation")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DataBindingUtil.bind(view)
        _binding?.lifecycleOwner = viewLifecycleOwner

        if (savedInstanceState == null) {
            onInit()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            onInit(savedInstanceState)
        }
    }

    private fun onInit(savedInstanceState: Bundle? = null) {
        initView(savedInstanceState)

        setOnClick()

        bindingStateView()

        bindingAction()
    }

    open fun setOnClick() {
        //do nothing
    }

    open fun initView(savedInstanceState: Bundle?) {
        //do nothing
    }

    open fun bindingStateView() {
        //do nothing
    }

    open fun bindingAction() {
        //do nothing
    }

    override fun onDestroyView() {
        _binding?.unbind()
        _binding = null
        super.onDestroyView()
    }


    fun showHideLoading(isShow: Boolean) {
        if (activity != null && activity is BaseActivityNotRequireViewModel<*>) {
            if (isShow) {
                (activity as BaseActivityNotRequireViewModel<*>?)!!.showLoading()
            } else {
                (activity as BaseActivityNotRequireViewModel<*>?)!!.hiddenLoading()
            }
        }
    }
}