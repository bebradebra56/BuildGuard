package com.buildfgu.guardbiu.data.repository

import com.buildfgu.guardbiu.data.local.AppDatabase
import com.buildfgu.guardbiu.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(private val db: AppDatabase) {

    // Projects
    fun getAllProjects(): Flow<List<ProjectEntity>> = db.projectDao().getAll()
    fun getProjectCount(): Flow<Int> = db.projectDao().getCount()
    suspend fun getProject(id: Long): ProjectEntity? = db.projectDao().getById(id)
    suspend fun insertProject(project: ProjectEntity): Long = db.projectDao().insert(project)
    suspend fun updateProject(project: ProjectEntity) = db.projectDao().update(project)
    suspend fun deleteProject(project: ProjectEntity) = db.projectDao().delete(project)

    // Rooms
    fun getAllRooms(): Flow<List<RoomEntity>> = db.roomDao().getAll()
    fun getRoomsByProject(projectId: Long): Flow<List<RoomEntity>> = db.roomDao().getByProject(projectId)
    fun getRecentRooms(): Flow<List<RoomEntity>> = db.roomDao().getRecent()
    fun getRoomCountByProject(projectId: Long): Flow<Int> = db.roomDao().getCountByProject(projectId)
    fun getTotalRoomCount(): Flow<Int> = db.roomDao().getTotalCount()
    fun getTotalArea(): Flow<Float> = db.roomDao().getTotalArea()
    suspend fun getRoom(id: Long): RoomEntity? = db.roomDao().getById(id)
    suspend fun insertRoom(room: RoomEntity): Long = db.roomDao().insert(room)
    suspend fun updateRoom(room: RoomEntity) = db.roomDao().update(room)
    suspend fun deleteRoom(room: RoomEntity) = db.roomDao().delete(room)

    // Wall Points
    fun getWallPoints(roomId: Long): Flow<List<WallPointEntity>> = db.wallPointDao().getByRoom(roomId)
    suspend fun getWallPointsOnce(roomId: Long): List<WallPointEntity> = db.wallPointDao().getByRoomOnce(roomId)
    suspend fun insertWallPoint(point: WallPointEntity): Long = db.wallPointDao().insert(point)
    suspend fun insertWallPoints(points: List<WallPointEntity>) = db.wallPointDao().insertAll(points)
    suspend fun deleteWallPoints(roomId: Long) = db.wallPointDao().deleteByRoom(roomId)

    // Room Elements
    fun getRoomElements(roomId: Long): Flow<List<RoomElementEntity>> = db.roomElementDao().getByRoom(roomId)
    suspend fun insertRoomElement(element: RoomElementEntity): Long = db.roomElementDao().insert(element)
    suspend fun deleteRoomElement(element: RoomElementEntity) = db.roomElementDao().delete(element)

    // Furniture
    fun getAllFurniture(): Flow<List<FurnitureEntity>> = db.furnitureDao().getAll()
    fun getFurnitureByRoom(roomId: Long): Flow<List<FurnitureEntity>> = db.furnitureDao().getByRoom(roomId)
    suspend fun getFurniture(id: Long): FurnitureEntity? = db.furnitureDao().getById(id)
    suspend fun insertFurniture(furniture: FurnitureEntity): Long = db.furnitureDao().insert(furniture)
    suspend fun updateFurniture(furniture: FurnitureEntity) = db.furnitureDao().update(furniture)
    suspend fun deleteFurniture(furniture: FurnitureEntity) = db.furnitureDao().delete(furniture)

    // Measurements
    fun getAllMeasurements(): Flow<List<MeasurementEntity>> = db.measurementDao().getAll()
    fun getMeasurementsByRoom(roomId: Long): Flow<List<MeasurementEntity>> = db.measurementDao().getByRoom(roomId)
    fun getTotalMeasurementCount(): Flow<Int> = db.measurementDao().getTotalCount()
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long = db.measurementDao().insert(measurement)
    suspend fun deleteMeasurement(measurement: MeasurementEntity) = db.measurementDao().delete(measurement)

    // Materials
    fun getAllMaterials(): Flow<List<MaterialEntity>> = db.materialDao().getAll()
    fun getMaterialCategories(): Flow<List<String>> = db.materialDao().getCategories()
    suspend fun insertMaterial(material: MaterialEntity): Long = db.materialDao().insert(material)
    suspend fun updateMaterial(material: MaterialEntity) = db.materialDao().update(material)
    suspend fun deleteMaterial(material: MaterialEntity) = db.materialDao().delete(material)

    // Shopping Items
    fun getShoppingItemsByProject(projectId: Long): Flow<List<ShoppingItemEntity>> = db.shoppingItemDao().getByProject(projectId)
    fun getAllShoppingItems(): Flow<List<ShoppingItemEntity>> = db.shoppingItemDao().getAll()
    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long = db.shoppingItemDao().insert(item)
    suspend fun updateShoppingItem(item: ShoppingItemEntity) = db.shoppingItemDao().update(item)
    suspend fun deleteShoppingItem(item: ShoppingItemEntity) = db.shoppingItemDao().delete(item)

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = db.taskDao().getAll()
    fun getTasksByDateRange(start: Long, end: Long): Flow<List<TaskEntity>> = db.taskDao().getByDateRange(start, end)
    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insert(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().update(task)
    suspend fun deleteTask(task: TaskEntity) = db.taskDao().delete(task)

    // Activity Logs
    fun getAllLogs(): Flow<List<ActivityLogEntity>> = db.activityLogDao().getAll()
    fun getRecentLogs(): Flow<List<ActivityLogEntity>> = db.activityLogDao().getRecent()
    suspend fun insertLog(log: ActivityLogEntity): Long = db.activityLogDao().insert(log)

    suspend fun clearCompletedShoppingItems(projectId: Long) =
        db.shoppingItemDao().deleteChecked(projectId)

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        db.clearAllTables()
    }

    suspend fun logActivity(projectId: Long = 0, action: String) {
        insertLog(ActivityLogEntity(projectId = projectId, action = action))
    }
}
