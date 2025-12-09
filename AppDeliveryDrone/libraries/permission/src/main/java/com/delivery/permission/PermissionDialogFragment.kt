package com.delivery.permission

import android.content.Context
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

class PermissionDialogFragment : DialogFragment() {
    private var permissionListener: PermissionListener? = null
    private var permissions: ArrayList<String>? = null
    private var requestCode: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissionListener = getPermissionListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    private fun initData() {
        permissions = arguments?.getStringArrayList(PERMISSIONS)
        requestCode = arguments?.getInt(REQUEST_CODE)
        when {
            permissions.isNullOrEmpty() -> {
                dismiss()
            }

            permissions?.size == 1 -> {
                setLauncherSingle()
            }

            else -> {
                setLauncherMultiple()
            }
        }
    }

    private fun setLauncherSingle() {
        permissions?.firstOrNull()?.let {
            val contract = ActivityResultContracts.RequestPermission()
            val launcher =
                registerForActivityResult(contract) { isGranted ->
                    handleActivityResult(isGranted, it)
                    dismiss()
                }
            launcher.launch(it)
        } ?: run {
            dismiss()
        }
    }

    private fun setLauncherMultiple() {
        permissions?.toTypedArray()?.let {
            val contract = ActivityResultContracts.RequestMultiplePermissions()
            val launcher =
                registerForActivityResult(contract) { mapPermission ->
                    handleActivityResult(mapPermission)
                    dismiss()
                }
            launcher.launch(it)
        } ?: run {
            dismiss()
        }
    }

    private fun handleActivityResult(
        isGranted: Boolean,
        permission: String,
    ) {
        val permissionStatus =
            requireActivity().checkPermissionsStatus(permission)
        if (isGranted) {
            permissionListener?.onPermissionGranted(requestCode)
        } else {
            permissionListener?.onPermissionDenied(
                requestCode,
                listOf(permissionStatus),
                listOf(permissionStatus).isDoNotAskAgain(),
            )
        }
    }

    private fun handleActivityResult(mapPermission: Map<String, @JvmSuppressWildcards Boolean>) {
        val permissionStatus =
            requireActivity().checkPermissionsStatus(mapPermission.keys.toList())
        if (permissionStatus.allGranted()) {
            permissionListener?.onPermissionGranted(requestCode)
        } else {
            permissionListener?.onPermissionDenied(
                requestCode,
                permissionStatus,
                permissionStatus.isDoNotAskAgain(),
            )
        }
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, getTagFragment())
    }

    private fun getTagFragment(): String {
        return PermissionDialogFragment::class.java.canonicalName
            ?: PermissionDialogFragment::class.java.simpleName
    }

    override fun onStart() {
        super.onStart()
        removeBackground()
    }

    private fun removeBackground() {
        dialog?.window?.setDimAmount(0f)
    }

    companion object {
        const val PERMISSIONS = "PERMISSIONS"
        const val REQUEST_CODE = "REQUEST_CODE"
    }
}

fun FragmentActivity.requestPermission(
    requestCode: Int,
    vararg permission: String,
) {
    PermissionDialogFragment().apply {
        arguments =
            bundleOf(
                PermissionDialogFragment.PERMISSIONS to ArrayList(permission.toList()),
                PermissionDialogFragment.REQUEST_CODE to requestCode,
            )
        show(supportFragmentManager)
    }
}

fun Fragment.requestPermission(
    requestCode: Int,
    vararg permission: String,
) {
    PermissionDialogFragment().apply {
        arguments =
            bundleOf(
                PermissionDialogFragment.PERMISSIONS to ArrayList(permission.toList()),
                PermissionDialogFragment.REQUEST_CODE to requestCode,
            )
        show(this@requestPermission.childFragmentManager)
    }
}
