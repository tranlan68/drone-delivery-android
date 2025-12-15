package com.delivery.setting.domain.usecase

import com.delivery.core.model.mocking.OrderCreateRequest
import com.delivery.core.model.mocking.OrderCreateResponse
import com.delivery.setting.domain.manager.UserManager
import com.delivery.setting.repository.OrderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateOrderUseCase
    @Inject
    constructor(
        private val orderRepository: OrderRepository,
        private val userManager: UserManager,
    ) {
        suspend fun execute(
            source: String,
            dest: String,
            weight: Int,
            size: Int,
            priority: Int = 1,
            receiverPhone: String,
        ): Result<OrderCreateResponse> {
            // Validate business rules
            if (source.isBlank() || dest.isBlank()) {
                return Result.failure(IllegalArgumentException("Source and destination cannot be empty"))
            }

            if (weight <= 0) {
                return Result.failure(IllegalArgumentException("Weight must be greater than 0"))
            }

            if (size !in 0..4) {
                return Result.failure(IllegalArgumentException("Size must be between 0 and 4"))
            }

            if (priority !in 0..2) {
                return Result.failure(IllegalArgumentException("Priority must be between 0 and 2"))
            }

//        if (receiverPhone.isBlank()) {
//            return Result.failure(IllegalArgumentException("Receiver phone cannot be empty"))
//        }

            // Get current user ID - this is a suspend function call
            val userCreateId = userManager.getCurrentUserIdSync() ?: "anonymous_user"

            // Create API request
            val request =
                OrderCreateRequest(
                    true,
                    source = source,
                    dest = dest,
                    userCreateId = "TrongVQ",
                    weight = weight,
                    size = size,
                    priority = priority,
                )

            // Call repository
            return orderRepository.createOrder(request)
        }
    }
