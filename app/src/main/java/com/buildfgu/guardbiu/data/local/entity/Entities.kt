package com.buildfgu.guardbiu.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val apartmentType: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val width: Float = 0f,
    val length: Float = 0f,
    val height: Float = 2.5f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wall_points")
data class WallPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val x: Float,
    val y: Float,
    val orderIndex: Int
)

@Entity(tableName = "room_elements")
data class RoomElementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val type: String,
    val wallIndex: Int,
    val positionOnWall: Float
)

@Entity(tableName = "furniture")
data class FurnitureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val name: String,
    val width: Float,
    val depth: Float,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val rotation: Float = 0f
)

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val label: String,
    val value: Float,
    val unit: String = "m"
)

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String,
    val pricePerUnit: Float,
    val category: String
)

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val quantity: Int = 1,
    val isChecked: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long = 0,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long = 0,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)
