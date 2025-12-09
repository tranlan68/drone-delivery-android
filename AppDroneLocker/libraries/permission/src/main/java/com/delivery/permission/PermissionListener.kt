package com.delivery.permission

interface PermissionListener {
    fun onPermissionGranted(requestCode: Int?)
    fun onPermissionDenied(
        requestCode: Int?,
        permissions: List<PermissionStatus>,
        isDoNotAskAgain: Boolean
    )
}