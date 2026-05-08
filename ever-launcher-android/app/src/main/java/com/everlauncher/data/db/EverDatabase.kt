package com.everlauncher.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.everlauncher.data.db.entities.AnalyticsEntity
import com.everlauncher.data.db.entities.AppEntity
import com.everlauncher.data.db.entities.ModeEntity

@Database(
    entities = [AppEntity::class, ModeEntity::class, AnalyticsEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EverDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun modeDao(): ModeDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile private var INSTANCE: EverDatabase? = null

        fun getInstance(context: Context): EverDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EverDatabase::class.java,
                    "ever_launcher.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
