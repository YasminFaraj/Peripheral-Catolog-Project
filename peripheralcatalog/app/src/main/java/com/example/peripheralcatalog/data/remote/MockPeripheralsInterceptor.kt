package com.example.peripheralcatalog.data.remote

import android.content.Context
import com.example.peripheralcatalog.R
import com.example.peripheralcatalog.data.remote.dto.PeripheralDto
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockPeripheralsInterceptor(context: Context) : Interceptor {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, PeripheralDto::class.java)
    private val listAdapter: JsonAdapter<List<PeripheralDto>> = moshi.adapter(listType)
    private val dtoAdapter: JsonAdapter<PeripheralDto> = moshi.adapter(PeripheralDto::class.java)
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter: JsonAdapter<List<String>> = moshi.adapter(stringListType)

    private val allPeripherals: List<PeripheralDto>
    private val allCategories: List<String>

    init {
        allPeripherals = context.resources.openRawResource(R.raw.peripherals).bufferedReader().use {
            listAdapter.fromJson(it.readText()).orEmpty()
        }
        allCategories = runCatching {
            context.resources.openRawResource(R.raw.categories).bufferedReader().use { reader ->
                stringListAdapter.fromJson(reader.readText()).orEmpty()
            }
        }.getOrElse {
            allPeripherals.map { dto -> dto.category }.distinct()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val pathSegments = request.url.pathSegments
        if (pathSegments.isEmpty()) {
            return notFoundResponse(request)
        }

        return when (pathSegments.first()) {
            "peripherals" -> handlePeripherals(request)
            "categories" -> jsonResponse(request, stringListAdapter.toJson(allCategories))
            else -> notFoundResponse(request)
        }
    }

    private fun handlePeripherals(request: Request): Response {
        val url = request.url
        val segments = url.pathSegments
        if (segments.size > 1) {
            val id = segments[1]
            val dto = allPeripherals.firstOrNull { it.id == id }
                ?: return notFoundResponse(request)
            return jsonResponse(request, dtoAdapter.toJson(dto))
        }

        val filtered = filterPeripherals(url)
        return jsonResponse(request, listAdapter.toJson(filtered))
    }

    private fun filterPeripherals(url: HttpUrl): List<PeripheralDto> {
        var current = allPeripherals
        url.queryParameter("category")?.takeIf { it.isNotBlank() }?.let { category ->
            current = current.filter { it.category.equals(category, ignoreCase = true) }
        }
        url.queryParameter("brand")?.takeIf { it.isNotBlank() }?.let { brand ->
            current = current.filter { it.brand.equals(brand, ignoreCase = true) }
        }
        url.queryParameter("search")?.takeIf { it.isNotBlank() }?.let { query ->
            val lowercase = query.lowercase()
            current = current.filter {
                it.name.lowercase().contains(lowercase) ||
                    it.brand.lowercase().contains(lowercase)
            }
        }
        url.queryParameter("minPrice")?.toDoubleOrNull()?.let { minPrice ->
            current = current.filter { it.price >= minPrice }
        }
        url.queryParameter("maxPrice")?.toDoubleOrNull()?.let { maxPrice ->
            current = current.filter { it.price <= maxPrice }
        }
        val features = url.queryParameterValues("feature")
        if (features.isNotEmpty()) {
            current = current.filter { dto ->
                features.all { feature ->
                    dto.features.any { it.equals(feature, ignoreCase = true) }
                }
            }
        }
        return current
    }

    private fun jsonResponse(request: Request, content: String): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = content.toResponseBody(mediaType)
        return Response.Builder()
            .code(200)
            .protocol(Protocol.HTTP_1_1)
            .message("OK")
            .request(request)
            .body(body)
            .build()
    }

    private fun notFoundResponse(request: Request): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = "{}".toResponseBody(mediaType)
        return Response.Builder()
            .code(404)
            .protocol(Protocol.HTTP_1_1)
            .message("Not Found")
            .request(request)
            .body(body)
            .build()
    }
}

