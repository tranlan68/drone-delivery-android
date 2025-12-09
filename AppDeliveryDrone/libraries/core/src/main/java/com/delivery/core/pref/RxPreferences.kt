package com.delivery.core.pref

import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
interface RxPreferences : BasePreferences {
    fun getToken(): Flow<String?>

    suspend fun setUserToken(userToken: String)

    fun getLanguage(): Flow<String?>

    suspend fun setLanguage(language: String)


    fun getProducts(): Flow<String?>

    suspend fun setProducts(products: String)

    fun getOrderProducts(): Flow<String?>

    suspend fun setOrderProducts(products: String)

    suspend fun logout()
}
