package com.buildfgu.guardbiu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.buildfgu.guardbiu.data.local.dao.*
import com.buildfgu.guardbiu.data.local.entity.*

@Database(
    entities = [
        ProjectEntity::class,
        RoomEntity::class,
        WallPointEntity::class,
        RoomElementEntity::class,
        FurnitureEntity::class,
        MeasurementEntity::class,
        MaterialEntity::class,
        ShoppingItemEntity::class,
        TaskEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun roomDao(): RoomDao
    abstract fun wallPointDao(): WallPointDao
    abstract fun roomElementDao(): RoomElementDao
    abstract fun furnitureDao(): FurnitureDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun materialDao(): MaterialDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun taskDao(): TaskDao
    abstract fun activityLogDao(): ActivityLogDao
}
