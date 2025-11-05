package com.example.peripheralcatalog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.peripheralcatalog.data.local.entity.PeripheralEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeripheralDao {

    @Query("SELECT * FROM peripherals ORDER BY name")
    fun observePeripherals(): Flow<List<PeripheralEntity>>

    @Query("SELECT * FROM peripherals WHERE id = :id")
    fun observePeripheral(id: String): Flow<PeripheralEntity?>

    @Query("SELECT * FROM peripherals")
    suspend fun getPeripheralsOnce(): List<PeripheralEntity>

    @Query("SELECT * FROM peripherals WHERE id = :id")
    suspend fun getPeripheral(id: String): PeripheralEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeripherals(peripherals: List<PeripheralEntity>)

    @Query("UPDATE peripherals SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT * FROM peripherals WHERE isFavorite = 1 ORDER BY name")
    fun observeFavorites(): Flow<List<PeripheralEntity>>

    @Query("SELECT id FROM peripherals WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<String>
}

