package com.example.peripheralcatalog.data.remote

import com.example.peripheralcatalog.data.remote.dto.PeripheralDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PeripheralApiService {

    @GET("peripherals")
    suspend fun getPeripherals(
        @Query("category") category: String? = null
    ): List<PeripheralDto>

    @GET("peripherals/{id}")
    suspend fun getPeripheral(
        @Path("id") id: String
    ): PeripheralDto

    @GET("categories")
    suspend fun getCategories(): List<String>
}

