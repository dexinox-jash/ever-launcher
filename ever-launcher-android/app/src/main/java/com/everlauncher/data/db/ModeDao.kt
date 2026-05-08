package com.everlauncher.data.db

import androidx.room.*
import com.everlauncher.data.db.entities.ModeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeDao {
    @Query("SELECT * FROM modes ORDER BY sort_order ASC, created_at ASC")
    fun getAllFlow(): Flow<List<ModeEntity>>

    @Query("SELECT * FROM modes ORDER BY sort_order ASC, created_at ASC")
    suspend fun getAll(): List<ModeEntity>

    @Query("SELECT * FROM modes WHERE id = :id")
    suspend fun getById(id: String): ModeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mode: ModeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(modes: List<ModeEntity>)

    @Update
    suspend fun update(mode: ModeEntity)

    @Delete
    suspend fun delete(mode: ModeEntity)

    @Query("DELETE FROM modes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM modes")
    suspend fun count(): Int
}
