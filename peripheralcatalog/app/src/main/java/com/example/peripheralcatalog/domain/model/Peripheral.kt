package com.example.peripheralcatalog.domain.model

data class Peripheral(
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val description: String,
    val specs: Map<String, String>,
    val features: List<String>,
    val isFavorite: Boolean
)

data class PeripheralHistoryItem(
    val peripheral: Peripheral,
    val viewedAt: Long
)

