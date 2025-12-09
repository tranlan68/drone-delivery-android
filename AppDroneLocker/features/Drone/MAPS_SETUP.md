# Google Maps Setup Guide

## 1. Lấy Google Maps API Key

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Enable **Maps SDK for Android** API
4. Tạo API Key:
   - Credentials → Create Credentials → API Key
   - Restrict API key để bảo mật (optional)

## 2. Cấu hình API Key

### Option 1: Sử dụng strings.xml (Hiện tại)
Thay thế `YOUR_GOOGLE_MAPS_API_KEY_HERE` trong file:
```xml
<!-- features/Demo/src/main/res/values/strings.xml -->
<string name="google_maps_key">YOUR_ACTUAL_API_KEY_HERE</string>
```

### Option 2: Sử dụng local.properties (Recommended)
1. Thêm vào `local.properties`:
```properties
MAPS_API_KEY=your_actual_api_key_here
```

2. Cập nhật `features/Demo/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""
    }
}
```

3. Cập nhật `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

## 3. Permissions

Đã được thêm vào `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

## 4. Test Maps

1. Build và chạy app
2. Navigate đến `DeliveryTrackingFragment`
3. Maps sẽ hiển thị với 2 markers và route line

## 5. Troubleshooting

### Map không hiển thị:
- Kiểm tra API key đúng chưa
- Kiểm tra internet connection
- Kiểm tra Maps SDK enabled trong Google Cloud Console

### Markers không hiển thị:
- Kiểm tra coordinates trong `SampleDeliveryData`
- Kiểm tra permissions location

### Route line không vẽ:
- Hiện tại dùng simplified route algorithm
- Để dùng real Google Directions API, cần implement `DirectionsApiService`

## 6. Navigation Usage

Để navigate đến màn hình tracking:
```kotlin
// Trong AppNavigatorImpl hoặc DemoNavigation implementation
fun navigateToDeliveryTracking(orderId: String) {
    val bundle = Bundle().apply {
        putString("orderId", orderId)
    }
    navController.navigate(R.id.deliveryTrackingFragment, bundle)
}
```
