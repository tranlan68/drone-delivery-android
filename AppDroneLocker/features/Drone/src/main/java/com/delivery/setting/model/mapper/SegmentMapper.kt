package com.delivery.setting.model.mapper

import com.delivery.core.model.network.mocking.OrderDto
import com.delivery.core.model.network.mocking.SegmentDto
import com.delivery.core.model.network.FlightLaneDetailDto
import com.delivery.core.model.network.DroneInfoDto
import com.delivery.core.pref.RxPreferences
import com.delivery.core.repositopry.LockerRepository
import com.delivery.setting.model.Segment
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SegmentMapper @Inject constructor(
    private val rxPreferences: RxPreferences,
    private val lockerRepository: LockerRepository
) {

    /**
     * Convert OrderDto to list of Segment models with filtering by currentLockerId
     */
    suspend fun mapOrdersToSegments(orders: List<OrderDto>, currentLockerId: String): List<Segment> {
        val segments = mutableListOf<Segment>()
        
        for (order in orders) {
            val orderSegments = order.segments ?: emptyList()
            
            // Filter segments that are related to current locker (source or dest)
            val filteredSegments = orderSegments.filter { segmentDto ->
                segmentDto.source == currentLockerId || segmentDto.dest == currentLockerId
            }
            
            for (segmentDto in filteredSegments) {
                val segment = mapSegmentDtoToSegment(segmentDto, order)
                segments.add(segment)
            }
        }
        
        return segments.sortedBy { it.segmentIndex }
    }
    
    /**
     * Convert SegmentDto to Segment model with additional info from OrderDto
     */
    private suspend fun mapSegmentDtoToSegment(segmentDto: SegmentDto, orderDto: OrderDto): Segment {
        val selectedLockerId = rxPreferences.getSelectedLockerId().first()
        // Get locker names by IDs
        val sourceName = if (!segmentDto.source.isNullOrEmpty()) {
            lockerRepository.getLockerNameById(segmentDto.source!!)
        } else "N/A"
        
        val destName = if (!segmentDto.dest.isNullOrEmpty()) {
            lockerRepository.getLockerNameById(segmentDto.dest!!)
        } else "N/A"
        
        return Segment(
            segmentIndex = segmentDto.segmentIndex ?: 0,
            source = segmentDto.source ?: "",
            dest = segmentDto.dest ?: "",
            flightLaneId = segmentDto.flightLaneId ?: "",
            droneId = segmentDto.droneId ?: "",
            status = segmentDto.segmentStatus ?: 0,
            orderId = orderDto.id,
            createdAt = orderDto.createdAt ?: 0L,
            sourceName = sourceName,
            destName = destName,
            lockerId = selectedLockerId,
            idSourceOrder = orderDto.source ?: "",
            idDestOrder = orderDto.dest ?: "",
        )
    }
    
    /**
     * Update segment with flight lane timing information
     */
    suspend fun updateSegmentWithTiming(
        segment: Segment, 
        flightLaneDetail: FlightLaneDetailDto
    ): Segment {
        val orderCreatedAt = segment.createdAt
        val positions = flightLaneDetail.position
        
        if (positions.isNotEmpty()) {
            // Calculate estimated start time: order.created_at + position[0].eta * 1000
            val firstPosition = positions.first()
            val estimatedStartTime = orderCreatedAt + (firstPosition.eta * 1000).toLong()
            
            // Calculate estimated end time: order.created_at + position[last].eta * 1000
            val lastPosition = positions.last()
            val estimatedEndTime = orderCreatedAt + (lastPosition.eta * 1000).toLong()
            
            return segment.copy(
                estimatedStartTime = estimatedStartTime,
                estimatedEndTime = estimatedEndTime
            )
        }
        
        return segment
    }
    
    /**
     * Update segment with drone GCS information
     */
    fun updateSegmentWithDroneInfo(segment: Segment, droneInfo: DroneInfoDto): Segment {
        return segment.copy(gcsId = droneInfo.gcsId)
    }
}
