package com.example.peripheralcatalog.data.remote.dto

data class PeripheralDto(
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val description: String,
    val specs: Map<String, String> = emptyMap(),
    val features: List<String> = emptyList()
)

