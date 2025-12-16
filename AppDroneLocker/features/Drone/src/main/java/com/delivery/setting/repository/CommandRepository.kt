package com.delivery.setting.repository

import com.delivery.core.base.BaseRepository
import com.delivery.core.model.network.CommandRequest
import com.delivery.core.model.network.CommandResponse
import com.delivery.core.model.network.CommandType
import com.delivery.core.network.ApiInterface
import com.delivery.setting.model.Segment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandRepository @Inject constructor(
    private val apiInterface: ApiInterface
) : BaseRepository() {

    suspend fun sendStartCommand(order: com.delivery.setting.model.Order, segmentIndex: Int): Flow<CommandResponse> = flow {
        try {
            val segment = order.segments.firstOrNull { it.segmentIndex == segmentIndex }
            val droneId = segment?.droneId ?: "1004" // fallback to hardcoded value
            
            val commandRequest = CommandRequest(
                orderId = order.id,
                commandType = CommandType.START.value,
                segmentIndex = segmentIndex,
                source = order.source,
                content = "start",
                droneId = droneId,
                updatedAt = order.updatedAt,
                createdAt = order.createdAt
            )
            
            Timber.d("CommandRepository: Sending START command for order: ${order.id}, segment: $segmentIndex, source: ${order.source}, droneId: $droneId")
            val response = apiInterface.sendCommand(commandRequest)
            Timber.d("CommandRepository: START command sent successfully: ${response.id}")
            
            emit(response)
        } catch (e: Exception) {
            Timber.e(e, "CommandRepository: Error sending START command for order: ${order.id}")
            throw e
        }
    }

    suspend fun sendFinishCommand(order: com.delivery.setting.model.Order, segmentIndex: Int): Flow<CommandResponse> = flow {
        try {
            val segment = order.segments.firstOrNull { it.segmentIndex == segmentIndex }
            val droneId = segment?.droneId ?: "1004" // fallback to hardcoded value
            
            val commandRequest = CommandRequest(
                orderId = order.id,
                commandType = CommandType.FINISH.value,
                segmentIndex = segmentIndex,
                source = order.source,
                content = "finish",
                droneId = droneId,
                updatedAt = order.updatedAt,
                createdAt = order.createdAt
            )
            
            Timber.d("CommandRepository: Sending FINISH command for order: ${order.id}, segment: $segmentIndex, source: ${order.source}, droneId: $droneId")
            val response = apiInterface.sendCommand(commandRequest)
            Timber.d("CommandRepository: FINISH command sent successfully: ${response.id}")
            
            emit(response)
        } catch (e: Exception) {
            Timber.e(e, "CommandRepository: Error sending FINISH command for order: ${order.id}")
            throw e
        }
    }

    suspend fun sendStartCommandForSegment(segment: Segment): Flow<CommandResponse> = flow {
        try {
            val commandRequest = CommandRequest(
                orderId = segment.orderId,
                commandType = CommandType.START.value,
                segmentIndex = segment.segmentIndex,
                source = segment.source,
                content = "start",
                droneId = segment.droneId,
                gcsId = segment.gcsId.ifEmpty { "gcs01" }, // Use segment's gcsId or fallback
                updatedAt = System.currentTimeMillis(),
                createdAt = segment.createdAt
            )
            
            Timber.d("CommandRepository: Sending START command for segment: ${segment.segmentIndex}, order: ${segment.orderId}, source: ${segment.source}, droneId: ${segment.droneId}, gcsId: ${segment.gcsId}")
            val response = apiInterface.sendCommand(commandRequest)
            Timber.d("CommandRepository: START command sent successfully: ${response.id}")
            
            emit(response)
        } catch (e: Exception) {
            Timber.e(e, "CommandRepository: Error sending START command for segment: ${segment.segmentIndex}")
            throw e
        }
    }

    suspend fun sendFinishCommandForSegment(segment: Segment): Flow<CommandResponse> = flow {
        try {
            val commandRequest = CommandRequest(
                orderId = segment.orderId,
                commandType = CommandType.FINISH.value,
                segmentIndex = segment.segmentIndex,
                source = segment.dest, // Use dest as source for FINISH command
                content = "finish",
                droneId = segment.droneId,
                gcsId = segment.gcsId.ifEmpty { "gcs01" }, // Use segment's gcsId or fallback
                updatedAt = System.currentTimeMillis(),
                createdAt = segment.createdAt
            )
            
            Timber.d("CommandRepository: Sending FINISH command for segment: ${segment.segmentIndex}, order: ${segment.orderId}, dest: ${segment.dest}, droneId: ${segment.droneId}, gcsId: ${segment.gcsId}")
            val response = apiInterface.sendCommand(commandRequest)
            Timber.d("CommandRepository: FINISH command sent successfully: ${response.id}")
            
            emit(response)
        } catch (e: Exception) {
            Timber.e(e, "CommandRepository: Error sending FINISH command for segment: ${segment.segmentIndex}")
            throw e
        }
    }
}
