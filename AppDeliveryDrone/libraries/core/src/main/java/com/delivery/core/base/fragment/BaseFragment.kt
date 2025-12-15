package com.delivery.core.base.fragment

import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.delivery.core.base.BaseViewModel

abstract class BaseFragment<BD : ViewDataBinding, VM : BaseViewModel>(
    @LayoutRes id: Int,
) :
    BaseFragmentNotRequireViewModel<BD>(id) {
    private lateinit var viewModel: VM

    abstract fun getVM(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getVM()
    }

    override fun initView(savedInstanceState: Bundle?) {
        viewModel.messageError.observe(viewLifecycleOwner) {
            var message = ""
            if (it is String) {
                message = it
            } else {
                if (it is Int) {
                    try {
                        message = getString(it)
                    } catch (e: Exception) {
                        // do nothing
                    }
                }
            }
            if (TextUtils.isEmpty(message)) return@observe
            //                showMessageError(message)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            showHideLoading(it)
        }
    }

    fun <T> doOnFragmentResult(
        key: String,
        action: (T) -> Unit,
    ) {
        findNavController().currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<T>(key).observe(viewLifecycleOwner) {
                action.invoke(it)
                remove<T>(key)
            }
        }
    }

    fun <T> doOnFragmentResult(
        @IdRes destinationId: Int,
        key: String,
        action: (T) -> Unit,
    ) {
        val navBackStackEntry = findNavController().getBackStackEntry(destinationId)
        navBackStackEntry.savedStateHandle
            .getLiveData<T>(key)
            .observe(viewLifecycleOwner) {
                action(it)
                navBackStackEntry.savedStateHandle.remove<T>(key)
            }
    }

    fun setFragmentResult(
        key: String,
        value: Any?,
    ) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(key, value)
    }

    fun setFragmentResult(
        @IdRes destinationId: Int,
        key: String,
        value: Any?,
    ) {
        val navBackStackEntry = findNavController().getBackStackEntry(destinationId)
        navBackStackEntry.savedStateHandle.set(key, value)
    }
}
