package com.delivery.uservht.domain.usecase

import com.delivery.core.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) {
        suspend fun execute(
            username: String,
            password: String,
        ): Result<String> {
            // Validate business rules
            if (username.isBlank()) {
                return Result.failure(IllegalArgumentException("Username cannot be empty"))
            }

            if (password.isBlank()) {
                return Result.failure(IllegalArgumentException("Password cannot be empty"))
            }

            return try {
                val token = authRepository.login(username, password)
                if (token.isNotEmpty()) {
                    Result.success(token)
                } else {
                    Result.failure(IllegalArgumentException("Invalid credentials"))
                }
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }
    }
