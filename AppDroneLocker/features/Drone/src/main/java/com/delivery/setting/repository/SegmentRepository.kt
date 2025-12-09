package com.delivery.setting.repository

import com.delivery.core.base.BaseRepository
import com.delivery.core.network.ApiInterface
import com.delivery.core.pref.RxPreferences
import com.delivery.setting.model.Segment
import com.delivery.setting.model.mapper.SegmentMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SegmentRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val rxPreferences: RxPreferences,
    private val segmentMapper: SegmentMapper
) : BaseRepository() {

    suspend fun getSegmentsList(): Flow<List<Segment>> = flow {
        val ordersDto = apiInterface.getOrdersList()
        val selectedLockerId = rxPreferences.getSelectedLockerId().first()
        
        Timber.d("SegmentRepository: Retrieved ${ordersDto.size} orders from API")
        Timber.d("SegmentRepository: Selected locker ID: $selectedLockerId")
        
        // If no locker selected, return empty list
        if (selectedLockerId.isNullOrEmpty()) {
            Timber.w("SegmentRepository: No locker selected, returning empty segments list")
            emit(emptyList())
            return@flow
        }
        
        // Convert orders to segments and filter by current locker
        val segments = segmentMapper.mapOrdersToSegments(ordersDto, selectedLockerId)
        
        Timber.d("SegmentRepository: Filtered to ${segments.size} segments for locker: $selectedLockerId")
        
        // Update segments with timing and drone info
        val enrichedSegments = mutableListOf<Segment>()
        for (segment in segments) {
            try {
                val enrichedSegment = enrichSegmentWithAdditionalInfo(segment)
                enrichedSegments.add(enrichedSegment)
            } catch (e: Exception) {
                Timber.w(e, "SegmentRepository: Failed to enrich segment ${segment.segmentIndex}, using basic info")
                enrichedSegments.add(segment)
            }
        }
        
        emit(enrichedSegments)
    }.catch { e ->
        Timber.e(e, "SegmentRepository: Error getting segments list")
        throw e
    }
    
    /**
     * Enrich segment with flight lane timing and drone GCS info
     */
    private suspend fun enrichSegmentWithAdditionalInfo(segment: Segment): Segment {
        var enrichedSegment = segment
        
        // Get flight lane detail for timing information
        if (segment.flightLaneId.isNotEmpty()) {
            try {
                val flightLaneDetail = apiInterface.getFlightLaneDetail(segment.flightLaneId)
                enrichedSegment = segmentMapper.updateSegmentWithTiming(enrichedSegment, flightLaneDetail)
                Timber.d("SegmentRepository: Updated segment ${segment.segmentIndex} with timing info")
            } catch (e: Exception) {
                Timber.w(e, "SegmentRepository: Failed to get flight lane detail for segment ${segment.segmentIndex}")
            }
        }
        
        // Get drone info for GCS ID
        if (segment.droneId.isNotEmpty()) {
            try {
                val droneInfo = apiInterface.getDroneInfo(segment.droneId)
                enrichedSegment = segmentMapper.updateSegmentWithDroneInfo(enrichedSegment, droneInfo)
                Timber.d("SegmentRepository: Updated segment ${segment.segmentIndex} with drone info: ${droneInfo.gcsId}")
            } catch (e: Exception) {
                Timber.w(e, "SegmentRepository: Failed to get drone info for segment ${segment.segmentIndex}")
            }
        }
        
        return enrichedSegment
    }
}
