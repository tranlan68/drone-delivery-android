package com.delivery.setting.domain.manager

import com.delivery.core.pref.RxPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager
    @Inject
    constructor(
        private val preferences: RxPreferences,
    ) {
        fun getCurrentUserId(): Flow<String?> {
            return preferences.getToken()
        }

        suspend fun getCurrentUserIdSync(): String? {
            return preferences.getToken().first()
        }

        suspend fun setCurrentUserId(userId: String) {
            preferences.setUserToken(userId)
        }

        suspend fun clearCurrentUser() {
            preferences.logout()
        }
    }
