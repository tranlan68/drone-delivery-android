package com.delivery.core.utils

object Constants {
    const val PREF_FILE_NAME = "Preferences"
    const val DEFAULT_TIMEOUT = 30
    const val DURATION_TIME_CLICKABLE = 500L

    object NetworkRequestCode {
        const val REQUEST_CODE_200 = 200 // normal
        const val REQUEST_CODE_400 = 400 // parameter error
        const val REQUEST_CODE_401 = 401 // unauthorized error
        const val REQUEST_CODE_403 = 403
        const val REQUEST_CODE_404 = 404 // No data error
        const val REQUEST_CODE_500 = 500 // system error
    }

    object ApiComponents {
        const val BASE_URL = "https://airtransys.site:9443"
        const val BASE_GOOGLE_MAP_URL = "https://google.com/"
    }

    object FragmentResultKeys {
        const val SELECTED_LOCKER = "selected_locker"
    }

    object LockerBundleKeys {
        const val LOCKER_ID = "locker_id"
        const val LOCKER_NAME = "locker_name"
        const val LOCKER_DESCRIPTION = "locker_description"
        const val LOCKER_LATITUDE = "locker_latitude"
        const val LOCKER_LONGITUDE = "locker_longitude"
        const val LOCATION_TYPE = "location_type"
        const val SELECTED_LOCKER_ID = "selected_locker_id"
        const val SELECTED_LOCKER_NAME = "selected_locker_name"
    }

    object BundleKeys {
        const val LOCATION_TYPE = "location_type"
        const val ORDER_DETAIL = "order_detail"
    }

    object LocationType {
        const val PICKUP = "pickup"
        const val DELIVERY = "delivery"
    }
}
