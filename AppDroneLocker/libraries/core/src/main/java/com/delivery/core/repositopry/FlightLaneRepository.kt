package com.delivery.core.repositopry

import com.delivery.core.base.BaseRepository
import com.delivery.core.model.network.FlightLaneDto
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
class FlightLaneRepository @Inject constructor(
    private val apiInterface: ApiInterface
) : BaseRepository() {

    /**
     * Lấy thông tin làn bay theo lane_id
     * Sử dụng ApiInterface để gọi API thực tế
     */
    suspend fun getFlightLaneById(laneId: String): Flow<List<FlightLaneDto>> = flow {
        try {
            // TODO: Uncomment khi muốn dùng API thực tế
             val response = apiInterface.getFlightLaneById(laneId)
             emit(response)

//            // Tạm thời sử dụng mock data để test UI
//            val mockData = getMockFlightLaneData(laneId)
//            emit(mockData)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching flight lane data for laneId: $laneId")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Mock data cho flight lane (tạm thời)
     */
    private suspend fun getMockFlightLaneData(laneId: String): List<FlightLaneDto> {
        // Simulate network delay
        delay(500)
        
        Timber.d("FlightLaneRepository.getMockFlightLaneData() - laneId: $laneId")

        // Mock data dựa trên lane_id
        return when (laneId) {
            "lane_001_segment_1" -> listOf(
                FlightLaneDto(
                    id = "lane_001_segment_1",
                    supplierId = "supplier_001",
                    points = listOf(
                        listOf(105.8542, 21.0285), // Lạc Long Quân
                        listOf(105.8400, 21.0250), // Điểm trung gian 1
                        listOf(105.8300, 21.0220), // Điểm trung gian 2
                        listOf(105.8200, 21.0200)  // Trung Chuyển Hub 1
                    ),
                    name = "Segment 1: Lạc Long Quân - Hub 1",
                    width = 50.0,
                    corridorId = "corridor_001_seg1",
                    createdBy = "system"
                )
            )
            "lane_001_segment_2" -> listOf(
                FlightLaneDto(
                    id = "lane_001_segment_2",
                    supplierId = "supplier_001",
                    points = listOf(
                        listOf(105.8200, 21.0200), // Trung Chuyển Hub 1
                        listOf(105.8100, 21.0180), // Điểm trung gian 3
                        listOf(105.8000, 21.0160), // Điểm trung gian 4
                        listOf(105.7936, 21.0136)  // Keangnam
                    ),
                    name = "Segment 2: Hub 1 - Keangnam",
                    width = 50.0,
                    corridorId = "corridor_001_seg2",
                    createdBy = "system"
                )
            )
            "current_1", "history_1" -> listOf(
                FlightLaneDto(
                    id = "lane_001",
                    supplierId = "supplier_001",
                    points = listOf(
                        listOf(105.8542, 21.0285), // Lạc Long Quân
                        listOf(105.7936, 21.0136), // Keangnam
                        listOf(105.8000, 21.0200), // Điểm trung gian
                        listOf(105.8100, 21.0150)  // Điểm trung gian
                    ),
                    name = "Lane Lạc Long Quân - Keangnam",
                    width = 50.0,
                    corridorId = "corridor_001",
                    createdBy = "system"
                )
            )
            "current_2", "history_2" -> listOf(
                FlightLaneDto(
                    id = "lane_002",
                    supplierId = "supplier_001",
                    points = listOf(
                        listOf(105.8342, 21.0185), // Nguyễn Du
                        listOf(105.7836, 21.0036), // Lotte Tower
                        listOf(105.7900, 21.0100), // Điểm trung gian
                        listOf(105.8000, 21.0050)  // Điểm trung gian
                    ),
                    name = "Lane Nguyễn Du - Lotte Tower",
                    width = 45.0,
                    corridorId = "corridor_002",
                    createdBy = "system"
                )
            )
            "current_3", "history_3" -> listOf(
                FlightLaneDto(
                    id = "lane_003",
                    supplierId = "supplier_001",
                    points = listOf(
                        listOf(105.8142, 21.0085), // Hoàng Hoa Thám
                        listOf(105.7736, 20.9936), // Times City
                        listOf(105.7800, 21.0000), // Điểm trung gian
                        listOf(105.7900, 20.9950)  // Điểm trung gian
                    ),
                    name = "Lane Hoàng Hoa Thám - Times City",
                    width = 55.0,
                    corridorId = "corridor_003",
                    createdBy = "system"
                )
            )
            "current_4", "history_4" -> listOf(
                FlightLaneDto(
                    id = "lane_004",
                    supplierId = "supplier_001",
                    points = listOf(
                        listOf(105.7942, 20.9985), // Trần Phú
                        listOf(105.7536, 20.9836), // Royal City
                        listOf(105.7600, 20.9900), // Điểm trung gian
                        listOf(105.7700, 20.9850)  // Điểm trung gian
                    ),
                    name = "Lane Trần Phú - Royal City",
                    width = 40.0,
                    corridorId = "corridor_004",
                    createdBy = "system"
                )
            )
            "current_5", "history_5" -> listOf(
                FlightLaneDto(
                    id = "lane_005",
                    supplierId = "supplier_001",
                    points = listOf(
                        listOf(105.7742, 20.9885), // Láng Hạ
                        listOf(105.7336, 20.9736), // Vincom Bà Triệu
                        listOf(105.7400, 20.9800), // Điểm trung gian
                        listOf(105.7500, 20.9750)  // Điểm trung gian
                    ),
                    name = "Lane Láng Hạ - Vincom Bà Triệu",
                    width = 60.0,
                    corridorId = "corridor_005",
                    createdBy = "system"
                )
            )
            else -> listOf(
                FlightLaneDto(
                    id = "lane_default",
                    supplierId = "supplier_001",
                    points = listOf(
                        listOf(105.8542, 21.0285), // Default start
                        listOf(105.7936, 21.0136), // Default end
                        listOf(105.8200, 21.0200), // Điểm trung gian
                        listOf(105.8300, 21.0150)  // Điểm trung gian
                    ),
                    name = "Default Lane",
                    width = 50.0,
                    corridorId = "corridor_default",
                    createdBy = "system"
                )
            )
        }
    }
}
