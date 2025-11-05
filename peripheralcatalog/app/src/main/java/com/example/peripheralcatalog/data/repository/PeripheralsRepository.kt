package com.example.peripheralcatalog.data.repository

import com.example.peripheralcatalog.data.local.dao.HistoryDao
import com.example.peripheralcatalog.data.local.dao.PeripheralDao
import com.example.peripheralcatalog.data.local.entity.HistoryEntity
import com.example.peripheralcatalog.data.local.entity.PeripheralEntity
import com.example.peripheralcatalog.data.remote.PeripheralApiService
import com.example.peripheralcatalog.data.remote.dto.PeripheralDto
import com.example.peripheralcatalog.domain.model.Peripheral
import com.example.peripheralcatalog.domain.model.PeripheralHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class PeripheralsRepository(
    private val api: PeripheralApiService,
    private val peripheralDao: PeripheralDao,
    private val historyDao: HistoryDao
) {

    val peripheralsFlow: Flow<List<Peripheral>> =
        peripheralDao.observePeripherals().map { entities ->
            entities.map { it.toDomain() }
        }

    fun peripheralFlow(id: String): Flow<Peripheral?> =
        peripheralDao.observePeripheral(id).map { entity -> entity?.toDomain() }

    val favoritesFlow: Flow<List<Peripheral>> =
        peripheralDao.observeFavorites().map { entities -> entities.map { it.toDomain() } }

    val historyFlow: Flow<List<PeripheralHistoryItem>> = combine(
        historyDao.observeHistory(),
        peripheralsFlow
    ) { history, peripherals ->
        val map = peripherals.associateBy { it.id }
        history.mapNotNull { entry ->
            map[entry.peripheralId]?.let { peripheral ->
                PeripheralHistoryItem(peripheral, entry.viewedAt)
            }
        }
    }

    suspend fun refresh(category: String? = null) {
        val remote = api.getPeripherals(category)
        val current = peripheralDao.getPeripheralsOnce().associateBy({ it.id }, { it.isFavorite })
        val timestamp = System.currentTimeMillis()
        val entities = remote.map { dto ->
            dto.toEntity(
                isFavorite = current[dto.id] ?: false,
                lastUpdated = timestamp
            )
        }
        peripheralDao.upsertPeripherals(entities)
    }

    suspend fun fetchCategories(): List<String> {
        return runCatching { api.getCategories() }
            .getOrElse {
                peripheralDao.getPeripheralsOnce()
                    .map { it.category }
                    .distinct()
                    .sorted()
            }
    }

    suspend fun toggleFavorite(id: String) {
        val current = peripheralDao.getPeripheral(id) ?: return
        peripheralDao.setFavorite(id, !current.isFavorite)
    }

    suspend fun setFavorite(id: String, favorite: Boolean) {
        peripheralDao.setFavorite(id, favorite)
    }

    suspend fun recordView(id: String) {
        val entry = HistoryEntity(peripheralId = id, viewedAt = System.currentTimeMillis())
        historyDao.upsert(entry)
    }

    suspend fun clearHistory() {
        historyDao.clear()
    }

    suspend fun getPeripheralOnce(id: String): Peripheral? {
        val local = peripheralDao.getPeripheral(id)
        if (local != null) {
            return local.toDomain()
        }
        val remote = runCatching { api.getPeripheral(id) }.getOrNull()
        return remote?.let { dto ->
            val entity = dto.toEntity(isFavorite = false, lastUpdated = System.currentTimeMillis())
            peripheralDao.upsertPeripherals(listOf(entity))
            entity.toDomain()
        }
    }

    suspend fun getPeripheralsByIds(ids: Set<String>): List<Peripheral> {
        if (ids.isEmpty()) return emptyList()
        val local = peripheralDao.getPeripheralsOnce()
        val map = local.associateBy { it.id }
        return ids.mapNotNull { map[it]?.toDomain() }
    }

    private fun PeripheralEntity.toDomain(): Peripheral = Peripheral(
        id = id,
        name = name,
        brand = brand,
        category = category,
        price = price,
        imageUrl = imageUrl,
        description = description,
        specs = specs,
        features = features,
        isFavorite = isFavorite
    )

    private fun PeripheralDto.toEntity(
        isFavorite: Boolean,
        lastUpdated: Long
    ): PeripheralEntity = PeripheralEntity(
        id = id,
        name = name,
        brand = brand,
        category = category,
        price = price,
        imageUrl = imageUrl,
        description = description,
        specs = specs,
        features = features,
        isFavorite = isFavorite,
        lastUpdated = lastUpdated
    )
}

