package com.delivery.permission

sealed class PermissionStatus(open val permission: String) {
    data class Granted(override val permission: String) : PermissionStatus(permission)

    sealed class Denied(permission: String) : PermissionStatus(permission) {
        data class Permanently(override val permission: String) : Denied(permission)

        data class ShouldShowRationale(override val permission: String) : Denied(permission)
    }
}
