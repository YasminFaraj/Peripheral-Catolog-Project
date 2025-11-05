package com.example.peripheralcatalog

import android.app.Application
import android.content.Context
import com.example.peripheralcatalog.data.local.PeripheralDatabase
import com.example.peripheralcatalog.data.remote.MockPeripheralsInterceptor
import com.example.peripheralcatalog.data.remote.PeripheralApiService
import com.example.peripheralcatalog.data.repository.PeripheralsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class PeripheralCatalogApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(MockPeripheralsInterceptor(context))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://peripheral.mock/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val database: PeripheralDatabase = PeripheralDatabase.build(context)

    private val apiService: PeripheralApiService = retrofit.create(PeripheralApiService::class.java)

    val repository: PeripheralsRepository = PeripheralsRepository(
        api = apiService,
        peripheralDao = database.peripheralDao(),
        historyDao = database.historyDao()
    )
}

