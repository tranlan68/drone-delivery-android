package com.delivery.core.repository

import com.delivery.core.network.AuthApiInterface
import com.delivery.core.pref.RxPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository
    @Inject
    constructor(
        private val authApiInterface: AuthApiInterface,
        private val rxPreferences: RxPreferences,
    ) {
        suspend fun login(
            username: String,
            password: String,
        ): String {
            val token: String = authApiInterface.loginUser(username, password)
            rxPreferences.setUserToken(token)
            return token
        }

        suspend fun logout() {
            authApiInterface.logoutUser()
            rxPreferences.setUserToken("")
        }
    }
