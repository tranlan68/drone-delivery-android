package com.delivery.setting.repository

import com.delivery.core.base.BaseRepository
import com.delivery.core.model.network.FlightLaneDto
import com.delivery.core.model.network.FlightPosition
import com.delivery.core.model.network.PolarVelocity
import com.delivery.core.network.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightLaneRepository
    @Inject
    constructor(
        private val apiInterface: ApiInterface,
    ) : BaseRepository() {
        /**
         * Lấy thông tin làn bay theo lane_id
         * Sử dụng ApiInterface để gọi API thực tế
         */
        suspend fun getFlightLaneById(laneId: String): Flow<FlightLaneDto?> =
            flow {
                try {
                    // TODO: Uncomment khi muốn dùng API thực tế
                    val response = apiInterface.getFlightLaneById(laneId)
                    emit(response)

//            // Tạm thời sử dụng mock data để test UI
//            val mockData = getMockFlightLaneData(laneId)
//            emit(mockData)
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching flight lane data for laneId: $laneId")
                    emit(null)
                }
            }.flowOn(Dispatchers.IO)

        /**
         * Mock data cho flight lane (tạm thời)
         */
        private suspend fun getMockFlightLaneData(laneId: String): FlightLaneDto? {
            // Simulate network delay
            delay(500)

            Timber.d("FlightLaneRepository.getMockFlightLaneData() - laneId: $laneId")

            // Mock data dựa trên lane_id
            return when (laneId) {
                "lane_001_segment_1" ->
                    FlightLaneDto(
                        id = "lane_001_segment_1",
                        offset = 10,
                        position =
                            listOf(
                                FlightPosition(
                                    longitude = 105.8542,
                                    latitude = 21.0285,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.029267842),
                                    eta = -120.0,
                                ),
                                FlightPosition(
                                    longitude = 105.8542,
                                    latitude = 21.0285,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.43951306),
                                    eta = -103.333336,
                                ),
                                FlightPosition(
                                    longitude = 105.8400,
                                    latitude = 21.0250,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 2.8114865),
                                    eta = 2.9227865,
                                ),
                                FlightPosition(
                                    longitude = 105.8200,
                                    latitude = 21.0200,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(),
                                    eta = 19.589453,
                                ),
                            ),
                        corridorId = "corridor_001_seg1",
                        startLockerId = "f7de2f41-6eaa-462c-9a87-3acd64d545c5",
                        endLockerId = "hub_001",
                        createdAt = 1758615823000,
                        updatedAt = 1758615823000,
                        createdBy = "system",
                    )
                "lane_001_segment_2" ->
                    FlightLaneDto(
                        id = "lane_001_segment_2",
                        offset = 10,
                        position =
                            listOf(
                                FlightPosition(
                                    longitude = 105.8200,
                                    latitude = 21.0200,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.029267842),
                                    eta = -120.0,
                                ),
                                FlightPosition(
                                    longitude = 105.8100,
                                    latitude = 21.0180,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.43951306),
                                    eta = -103.333336,
                                ),
                                FlightPosition(
                                    longitude = 105.8000,
                                    latitude = 21.0160,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 2.8114865),
                                    eta = 2.9227865,
                                ),
                                FlightPosition(
                                    longitude = 105.7936,
                                    latitude = 21.0136,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(),
                                    eta = 19.589453,
                                ),
                            ),
                        corridorId = "corridor_001_seg2",
                        startLockerId = "hub_001",
                        endLockerId = "kiob1",
                        createdAt = 1758615823000,
                        updatedAt = 1758615823000,
                        createdBy = "system",
                    )
                "current_1", "history_1" ->
                    FlightLaneDto(
                        id = "lane_001",
                        offset = 10,
                        position =
                            listOf(
                                FlightPosition(
                                    longitude = 105.8542,
                                    latitude = 21.0285,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.029267842),
                                    eta = -120.0,
                                ),
                                FlightPosition(
                                    longitude = 105.8000,
                                    latitude = 21.0200,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.43951306),
                                    eta = -103.333336,
                                ),
                                FlightPosition(
                                    longitude = 105.8100,
                                    latitude = 21.0150,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 2.8114865),
                                    eta = 2.9227865,
                                ),
                                FlightPosition(
                                    longitude = 105.7936,
                                    latitude = 21.0136,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(),
                                    eta = 19.589453,
                                ),
                            ),
                        corridorId = "corridor_001",
                        startLockerId = "f7de2f41-6eaa-462c-9a87-3acd64d545c5",
                        endLockerId = "kiob1",
                        createdAt = 1758615823000,
                        updatedAt = 1758615823000,
                        createdBy = "system",
                    )
                "current_2", "history_2" ->
                    FlightLaneDto(
                        id = "lane_002",
                        offset = 10,
                        position =
                            listOf(
                                FlightPosition(
                                    longitude = 105.8342,
                                    latitude = 21.0185,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.029267842),
                                    eta = -120.0,
                                ),
                                FlightPosition(
                                    longitude = 105.7900,
                                    latitude = 21.0100,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.43951306),
                                    eta = -103.333336,
                                ),
                                FlightPosition(
                                    longitude = 105.8000,
                                    latitude = 21.0050,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 2.8114865),
                                    eta = 2.9227865,
                                ),
                                FlightPosition(
                                    longitude = 105.7836,
                                    latitude = 21.0036,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(),
                                    eta = 19.589453,
                                ),
                            ),
                        corridorId = "corridor_002",
                        startLockerId = "nguyen_du_locker",
                        endLockerId = "lotte_tower_locker",
                        createdAt = 1758615823000,
                        updatedAt = 1758615823000,
                        createdBy = "system",
                    )
                "current_3", "history_3" ->
                    FlightLaneDto(
                        id = "lane_003",
                        offset = 10,
                        position =
                            listOf(
                                FlightPosition(
                                    longitude = 105.8142,
                                    latitude = 21.0085,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.029267842),
                                    eta = -120.0,
                                ),
                                FlightPosition(
                                    longitude = 105.7800,
                                    latitude = 21.0000,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.43951306),
                                    eta = -103.333336,
                                ),
                                FlightPosition(
                                    longitude = 105.7900,
                                    latitude = 20.9950,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 2.8114865),
                                    eta = 2.9227865,
                                ),
                                FlightPosition(
                                    longitude = 105.7736,
                                    latitude = 20.9936,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(),
                                    eta = 19.589453,
                                ),
                            ),
                        corridorId = "corridor_003",
                        startLockerId = "hoang_hoa_tham_locker",
                        endLockerId = "times_city_locker",
                        createdAt = 1758615823000,
                        updatedAt = 1758615823000,
                        createdBy = "system",
                    )
                "current_4", "history_4" ->
                    FlightLaneDto(
                        id = "lane_004",
                        offset = 10,
                        position =
                            listOf(
                                FlightPosition(
                                    longitude = 105.7942,
                                    latitude = 20.9985,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.029267842),
                                    eta = -120.0,
                                ),
                                FlightPosition(
                                    longitude = 105.7600,
                                    latitude = 20.9900,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.43951306),
                                    eta = -103.333336,
                                ),
                                FlightPosition(
                                    longitude = 105.7700,
                                    latitude = 20.9850,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 2.8114865),
                                    eta = 2.9227865,
                                ),
                                FlightPosition(
                                    longitude = 105.7536,
                                    latitude = 20.9836,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(),
                                    eta = 19.589453,
                                ),
                            ),
                        corridorId = "corridor_004",
                        startLockerId = "tran_phu_locker",
                        endLockerId = "royal_city_locker",
                        createdAt = 1758615823000,
                        updatedAt = 1758615823000,
                        createdBy = "system",
                    )
                "current_5", "history_5" ->
                    FlightLaneDto(
                        id = "lane_005",
                        offset = 10,
                        position =
                            listOf(
                                FlightPosition(
                                    longitude = 105.7742,
                                    latitude = 20.9885,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.029267842),
                                    eta = -120.0,
                                ),
                                FlightPosition(
                                    longitude = 105.7400,
                                    latitude = 20.9800,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.43951306),
                                    eta = -103.333336,
                                ),
                                FlightPosition(
                                    longitude = 105.7500,
                                    latitude = 20.9750,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 2.8114865),
                                    eta = 2.9227865,
                                ),
                                FlightPosition(
                                    longitude = 105.7336,
                                    latitude = 20.9736,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(),
                                    eta = 19.589453,
                                ),
                            ),
                        corridorId = "corridor_005",
                        startLockerId = "lang_ha_locker",
                        endLockerId = "vincom_ba_trieu_locker",
                        createdAt = 1758615823000,
                        updatedAt = 1758615823000,
                        createdBy = "system",
                    )
                else ->
                    FlightLaneDto(
                        id = "lane_default",
                        offset = 10,
                        position =
                            listOf(
                                FlightPosition(
                                    longitude = 105.8542,
                                    latitude = 21.0285,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.029267842),
                                    eta = -120.0,
                                ),
                                FlightPosition(
                                    longitude = 105.8200,
                                    latitude = 21.0200,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 0.43951306),
                                    eta = -103.333336,
                                ),
                                FlightPosition(
                                    longitude = 105.8300,
                                    latitude = 21.0150,
                                    altitude = 100.0,
                                    polarVelocity = PolarVelocity(speed = 6.0, heading = 2.8114865),
                                    eta = 2.9227865,
                                ),
                                FlightPosition(
                                    longitude = 105.7936,
                                    latitude = 21.0136,
                                    altitude = 0.0,
                                    polarVelocity = PolarVelocity(),
                                    eta = 19.589453,
                                ),
                            ),
                        corridorId = "corridor_default",
                        startLockerId = "default_start_locker",
                        endLockerId = "default_end_locker",
                        createdAt = 1758615823000,
                        updatedAt = 1758615823000,
                        createdBy = "system",
                    )
            }
        }
    }
