package com.delivery.core.network

import com.delivery.core.utils.Constants.NetworkRequestCode.REQUEST_CODE_400
import com.delivery.core.utils.Constants.NetworkRequestCode.REQUEST_CODE_401
import com.delivery.core.utils.Constants.NetworkRequestCode.REQUEST_CODE_403
import com.delivery.core.utils.Constants.NetworkRequestCode.REQUEST_CODE_404
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.nio.charset.Charset
import javax.inject.Inject

class NetworkInterceptor
    @Inject
    constructor(
        private val gson: Gson,
        private val networkEvent: NetworkEvent,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            val responseBody = response.body

            try {
                when (response.code) {
                    REQUEST_CODE_400, REQUEST_CODE_401, REQUEST_CODE_403, REQUEST_CODE_404 -> {
                        val source = responseBody?.source()
                        source?.request(Long.MAX_VALUE)
                        val buffer = source?.buffer
                        val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8"))
                        val errorResponse = gson.fromJson(responseBodyString, ApiException::class.java)
                        Timber.d("network error code ${response.code}")
                        networkEvent.publish(NetworkState.GENERIC(errorResponse))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

            return response
        }
    }
