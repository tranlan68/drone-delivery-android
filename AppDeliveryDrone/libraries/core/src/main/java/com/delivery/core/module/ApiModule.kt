package com.delivery.core.module

import android.content.Context
import android.text.TextUtils
import com.delivery.core.network.ApiInterface
import com.delivery.core.network.AuthApiInterface
import com.delivery.core.network.DirectionsApiService
import com.delivery.core.network.NetworkEvent
import com.delivery.core.network.NetworkInterceptor
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.Constants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun providerNetworkEvent() = NetworkEvent()

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().setLenient().create()
    }

    @Provides
    @Singleton
    fun providerNetworkInterceptor(
        gson: Gson,
        networkEvent: NetworkEvent,
    ) = NetworkInterceptor(
        gson,
        networkEvent,
    )

    @Provides
    @Singleton
    fun provideApiInterface(
        gson: Gson,
        client: OkHttpClient,
    ): ApiInterface {
        val retrofit =
            Retrofit.Builder()
                .baseUrl(Constants.ApiComponents.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        return retrofit.create(ApiInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideDirectionsApiService(
        gson: Gson,
        client: OkHttpClient,
    ): DirectionsApiService {
        val retrofit =
            Retrofit.Builder()
                .baseUrl(Constants.ApiComponents.BASE_GOOGLE_MAP_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        return retrofit.create(DirectionsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApiInterface(
        gson: Gson,
        client: OkHttpClient,
    ): AuthApiInterface {
        val retrofit =
            Retrofit.Builder()
                .baseUrl(Constants.ApiComponents.BASE_URL)
                .client(client)
                .addConverterFactory(NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        return retrofit.create(AuthApiInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        cache: Cache?,
        rxPreferences: RxPreferences,
        networkInterceptor: NetworkInterceptor,
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(
                Interceptor { chain: Interceptor.Chain ->
                    val token =
                        runBlocking {
                            rxPreferences.getToken().first()
                        }
                    val request =
                        if (!TextUtils.isEmpty(token)) {
                            chain.request()
                                .newBuilder()
                                .header("Content-Type", "application/json")
                                .addHeader(
                                    "Authorization",
                                    token!!,
                                )
                                .build()
                        } else {
                            chain.request()
                                .newBuilder()
                                .header("Content-Type", "application/json")
                                .build()
                        }

                    chain.proceed(request)
                },
            )
            .addInterceptor(loggingInterceptor)
            .addInterceptor(networkInterceptor)
            .connectTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(Constants.DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideCache(
        @ApplicationContext application: Context,
    ): Cache {
        val cacheSize = 10 * 1024 * 1024.toLong() // 10 MB
        val httpCacheDirectory = File(application.cacheDir, "http-cache")
        return Cache(httpCacheDirectory, cacheSize)
    }

    @Provides
    fun provideGsonClient(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }
}

class NullOnEmptyConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val delegate: Converter<ResponseBody, Any> =
            retrofit.nextResponseBodyConverter(this, type, annotations)
        return Converter { body -> if (body.contentLength() == 0L) null else delegate.convert(body) }
    }
}
