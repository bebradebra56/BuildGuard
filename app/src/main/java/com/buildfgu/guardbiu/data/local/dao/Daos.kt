package com.buildfgu.guardbiu.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.buildfgu.guardbiu.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Query("SELECT COUNT(*) FROM projects")
    fun getCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY projectId, createdAt DESC")
    fun getAll(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getByProject(projectId: Long): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getById(id: Long): RoomEntity?

    @Query("SELECT * FROM rooms ORDER BY createdAt DESC LIMIT 5")
    fun getRecent(): Flow<List<RoomEntity>>

    @Query("SELECT COUNT(*) FROM rooms WHERE projectId = :projectId")
    fun getCountByProject(projectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM rooms")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(width * length), 0) FROM rooms")
    fun getTotalArea(): Flow<Float>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: RoomEntity): Long

    @Update
    suspend fun update(room: RoomEntity)

    @Delete
    suspend fun delete(room: RoomEntity)
}

@Dao
interface WallPointDao {
    @Query("SELECT * FROM wall_points WHERE roomId = :roomId ORDER BY orderIndex")
    fun getByRoom(roomId: Long): Flow<List<WallPointEntity>>

    @Query("SELECT * FROM wall_points WHERE roomId = :roomId ORDER BY orderIndex")
    suspend fun getByRoomOnce(roomId: Long): List<WallPointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: WallPointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<WallPointEntity>)

    @Query("DELETE FROM wall_points WHERE roomId = :roomId")
    suspend fun deleteByRoom(roomId: Long)
}

@Dao
interface RoomElementDao {
    @Query("SELECT * FROM room_elements WHERE roomId = :roomId")
    fun getByRoom(roomId: Long): Flow<List<RoomElementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(element: RoomElementEntity): Long

    @Delete
    suspend fun delete(element: RoomElementEntity)

    @Query("DELETE FROM room_elements WHERE roomId = :roomId")
    suspend fun deleteByRoom(roomId: Long)
}

@Dao
interface FurnitureDao {
    @Query("SELECT * FROM furniture ORDER BY roomId")
    fun getAll(): Flow<List<FurnitureEntity>>

    @Query("SELECT * FROM furniture WHERE roomId = :roomId")
    fun getByRoom(roomId: Long): Flow<List<FurnitureEntity>>

    @Query("SELECT * FROM furniture WHERE id = :id")
    suspend fun getById(id: Long): FurnitureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(furniture: FurnitureEntity): Long

    @Update
    suspend fun update(furniture: FurnitureEntity)

    @Delete
    suspend fun delete(furniture: FurnitureEntity)
}

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY roomId")
    fun getAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE roomId = :roomId")
    fun getByRoom(roomId: Long): Flow<List<MeasurementEntity>>

    @Query("SELECT COUNT(*) FROM measurements")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: MeasurementEntity): Long

    @Delete
    suspend fun delete(measurement: MeasurementEntity)
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY category, name")
    fun getAll(): Flow<List<MaterialEntity>>

    @Query("SELECT DISTINCT category FROM materials")
    fun getCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(material: MaterialEntity): Long

    @Update
    suspend fun update(material: MaterialEntity)

    @Delete
    suspend fun delete(material: MaterialEntity)
}

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items WHERE projectId = :projectId ORDER BY isChecked, id DESC")
    fun getByProject(projectId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items ORDER BY isChecked, id DESC")
    fun getAll(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity): Long

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Delete
    suspend fun delete(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE projectId = :projectId AND isChecked = 1")
    suspend fun deleteChecked(projectId: Long)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted, date")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date BETWEEN :start AND :end ORDER BY date")
    fun getByDateRange(start: Long, end: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecent(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLogEntity): Long

    @Query("DELETE FROM activity_logs WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
