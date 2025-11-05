package com.example.peripheralcatalog.data.local

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class PeripheralsConverters {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    private val mapAdapter = moshi.adapter<Map<String, String>>(mapType)
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val listAdapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun specsToJson(value: Map<String, String>?): String {
        return mapAdapter.toJson(value.orEmpty())
    }

    @TypeConverter
    fun jsonToSpecs(value: String?): Map<String, String> {
        if (value.isNullOrBlank()) return emptyMap()
        return runCatching { mapAdapter.fromJson(value).orEmpty() }.getOrDefault(emptyMap())
    }

    @TypeConverter
    fun featuresToJson(value: List<String>?): String {
        return listAdapter.toJson(value.orEmpty())
    }

    @TypeConverter
    fun jsonToFeatures(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return runCatching { listAdapter.fromJson(value).orEmpty() }.getOrDefault(emptyList())
    }
}

