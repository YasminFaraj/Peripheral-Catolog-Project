package com.example.peripheralcatalog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peripherals")
data class PeripheralEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val description: String,
    val specs: Map<String, String>,
    val features: List<String>,
    val isFavorite: Boolean,
    val lastUpdated: Long
)

