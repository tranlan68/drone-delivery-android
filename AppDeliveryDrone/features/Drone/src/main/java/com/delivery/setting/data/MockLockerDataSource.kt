package com.delivery.setting.data

import com.delivery.core.model.network.Locker

/**
 * Mock data source for lockers
 * TODO: Remove this when API is ready and use real API calls
 */
object MockLockerDataSource {
    fun getMockLockers(): List<Locker> =
        listOf(
            Locker(
                id = "location1",
                lockerName = "380 Lạc Long Quân",
                description = "Tòa nhà Viettel, 380 Đường Lạc Long Quân, Tây Hồ",
                position = listOf(105.8114404, 21.0677385), // Exact coordinates from Google Maps
                createdBy = "system",
            ),
            Locker(
                id = "location2",
                lockerName = "Hồ Tây",
                description = "Hồ Tây – West Lake",
                position = listOf(105.8235, 21.0490), // West Lake center
                createdBy = "system",
            ),
            Locker(
                id = "location3",
                lockerName = "Hồ Hoàn Kiếm",
                description = "Hồ Hoàn Kiếm – Lake of Restored Sword",
                position = listOf(105.8525, 21.0285), // Hoan Kiem Lake center
                createdBy = "system",
            ),
            Locker(
                id = "location4",
                lockerName = "Văn Miếu – Quốc Tử Giám",
                description = "Temple of Literature",
                position = listOf(105.8350, 21.0278), // Temple of Literature
                createdBy = "system",
            ),
            Locker(
                id = "location5",
                lockerName = "Lăng Bác",
                description = "Lăng Chủ tịch Hồ Chí Minh",
                position = listOf(105.8342, 21.0307), // Ho Chi Minh Mausoleum
                createdBy = "system",
            ),
            Locker(
                id = "location6",
                lockerName = "Cầu Thăng Long",
                description = "Cầu Thăng Long",
                position = listOf(105.7710, 21.0570), // Thang Long Bridge
                createdBy = "system",
            ),
            Locker(
                id = "location7",
                lockerName = "Chùa Một Cột",
                description = "One Pillar Pagoda",
                position = listOf(105.8340, 21.0277), // One Pillar Pagoda
                createdBy = "system",
            ),
            Locker(
                id = "location8",
                lockerName = "Nhà hát Lớn Hà Nội",
                description = "Hanoi Opera House",
                position = listOf(105.8412, 21.0292), // Hanoi Opera House
                createdBy = "system",
            ),
            Locker(
                id = "location9",
                lockerName = "Trúc Bạch",
                description = "Trúc Bạch Lake area",
                position = listOf(105.8220, 21.0430), // Truc Bach Lake
                createdBy = "system",
            ),
            Locker(
                id = "location10",
                lockerName = "Cầu Long Biên",
                description = "Long Biên Bridge",
                position = listOf(105.8625, 21.0395), // Long Bien Bridge
                createdBy = "system",
            ),
        )
}
