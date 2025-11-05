package com.example.peripheralcatalog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.peripheralcatalog.data.local.dao.HistoryDao
import com.example.peripheralcatalog.data.local.dao.PeripheralDao
import com.example.peripheralcatalog.data.local.entity.HistoryEntity
import com.example.peripheralcatalog.data.local.entity.PeripheralEntity

@Database(
    entities = [PeripheralEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(PeripheralsConverters::class)
abstract class PeripheralDatabase : RoomDatabase() {

    abstract fun peripheralDao(): PeripheralDao
    abstract fun historyDao(): HistoryDao

    companion object {
        fun build(context: Context): PeripheralDatabase {
            return Room.databaseBuilder(
                context,
                PeripheralDatabase::class.java,
                "peripherals.db"
            ).build()
        }
    }
}

