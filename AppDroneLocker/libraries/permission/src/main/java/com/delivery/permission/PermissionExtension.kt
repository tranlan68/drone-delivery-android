package com.delivery.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Activity.checkPermissionsStatus(permissions: List<String>): List<PermissionStatus> {
    return permissions.map { permission ->
        checkPermissionsStatus(permission)
    }
}

fun Activity.checkPermissionsStatus(permission: String): PermissionStatus {
    return when {
        isPermissionGranted(permission) -> {
            PermissionStatus.Granted(permission)
        }

        shouldShowRequestPermissionRationale(this, permission) -> {
            PermissionStatus.Denied.ShouldShowRationale(permission)
        }

        else -> {
            PermissionStatus.Denied.Permanently(permission)
        }
    }
}

fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun List<PermissionStatus>.allGranted(): Boolean = all { it is PermissionStatus.Granted }

fun List<PermissionStatus>.isDoNotAskAgain(): Boolean =
    any { it is PermissionStatus.Denied.Permanently }

fun List<PermissionStatus>.getListGranted(): List<String> {
    return filterIsInstance<PermissionStatus.Granted>().map { it.permission }
}

fun List<PermissionStatus>.getListShouldShowRationale(): List<String> {
    return filterIsInstance<PermissionStatus.Denied.ShouldShowRationale>().map { it.permission }
}

fun List<PermissionStatus>.getListPermanently(): List<String> {
    return filterIsInstance<PermissionStatus.Denied.Permanently>().map { it.permission }
}

fun PermissionDialogFragment.getPermissionListener(): PermissionListener? {
    return try {
        if (parentFragment != null && parentFragment is PermissionListener) {
            parentFragment as PermissionListener
        } else if (activity is PermissionListener) {
            activity as PermissionListener
        } else {
            null
        }
    } catch (e: ClassCastException) {


        null
    }
}

fun Activity.openAppSetting() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    })
}

fun Fragment.openAppSetting() {
    requireActivity().openAppSetting()
}