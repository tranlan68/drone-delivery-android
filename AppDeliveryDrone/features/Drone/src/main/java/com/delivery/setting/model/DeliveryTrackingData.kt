package com.delivery.setting.model

import com.google.android.gms.maps.model.LatLng

data class DeliveryTrackingData(
    val orderId: String,
    val pickupLocation: LocationInfo,
    val deliveryLocation: LocationInfo,
    val deliveryStatus: DeliveryStatus,
    val packageInfo: PackageInfo,
    val completedDate: String? = null,
)

data class LocationInfo(
    val lockerName: String, // Tên locker
    val address: String, // Địa chỉ locker
    val latLng: LatLng,
)

data class PackageInfo(
    val type: String, // "Loại hàng"
    val size: String, // "S"
    val weight: String, // "1 kg"
)

enum class DeliveryStatus {
    PENDING,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED,
}

// Sample data matching the UI
object SampleDeliveryData {
    val currentDelivery =
        DeliveryTrackingData(
            orderId = "DL001",
            pickupLocation =
                LocationInfo(
                    lockerName = "Locker Lạc Long Quân",
                    address = "380 Lạc Long Quân",
                    latLng = LatLng(21.0285, 105.8542), // Hanoi coordinates
                ),
            deliveryLocation =
                LocationInfo(
                    lockerName = "Locker Keangnam",
                    address = "Keangnam Landmark 72",
                    latLng = LatLng(21.0136, 105.7936), // Keangnam coordinates
                ),
            deliveryStatus = DeliveryStatus.IN_TRANSIT,
            packageInfo =
                PackageInfo(
                    type = "Loại hàng",
                    size = "S",
                    weight = "1 kg",
                ),
            completedDate = "04 Aug",
        )
}
