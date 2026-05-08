package com.everlauncher.data.db

import androidx.room.*
import com.everlauncher.data.db.entities.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY sort_order ASC")
    fun getAllFlow(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY sort_order ASC")
    suspend fun getAll(): List<AppEntity>

    @Query("SELECT * FROM apps WHERE id = :id")
    suspend fun getById(id: String): AppEntity?

    @Query("SELECT * FROM apps WHERE package_name = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): AppEntity?

    @Query("SELECT * FROM apps WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<AppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<AppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: AppEntity)

    @Update
    suspend fun update(app: AppEntity)

    @Delete
    suspend fun delete(app: AppEntity)

    @Query("DELETE FROM apps WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM apps")
    suspend fun deleteAll()
}
