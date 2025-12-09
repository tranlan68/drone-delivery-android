package com.delivery.setting.repository

import com.delivery.core.base.BaseRepository
import com.delivery.core.network.ApiInterface
import com.delivery.core.pref.RxPreferences
import com.delivery.setting.model.Order
import com.delivery.setting.model.mapper.OrderMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val rxPreferences: RxPreferences,
    private val orderMapper: OrderMapper
) : BaseRepository() {

    suspend fun getOrdersList(): Flow<List<Order>> = flow {
        val ordersDto = apiInterface.getOrdersList()
        val selectedLockerId = rxPreferences.getSelectedLockerId().first()
        
        Timber.d("OrderRepository: Retrieved ${ordersDto.size} orders from API")
        Timber.d("OrderRepository: Selected locker ID: $selectedLockerId")
        
        // If no locker selected, return all orders (for testing purposes)
        val filteredOrders = if (selectedLockerId.isNullOrEmpty()) {
            Timber.w("OrderRepository: No locker selected, returning all orders for testing")
            ordersDto
        } else {
            ordersDto.filter { orderDto ->
                val isOrderSourceOrDest = orderDto.source == selectedLockerId || orderDto.dest == selectedLockerId
                val hasSegmentSourceOrDest = orderDto.segments?.any { segment ->
                    segment.source == selectedLockerId || segment.dest == selectedLockerId
                } ?: false
                
                val isFiltered = isOrderSourceOrDest || hasSegmentSourceOrDest
                
                if (isFiltered) {
                    Timber.d("OrderRepository: Order ${orderDto.id} matches filter - source: ${orderDto.source}, dest: ${orderDto.dest}")
                }
                
                isFiltered
            }
        }
        
        Timber.d("OrderRepository: Filtered to ${filteredOrders.size} orders")
        
        // Use OrderMapper to convert OrderDto to Order
        val orders = mutableListOf<Order>()
        for (orderDto in filteredOrders) {
            orders.add(orderMapper.mapToOrder(orderDto))
        }
        
        emit(orders)
    }.catch { e ->
        Timber.e(e, "OrderRepository: Error getting orders list")
        // Don't emit in catch block - let the error propagate
        throw e
    }
}
