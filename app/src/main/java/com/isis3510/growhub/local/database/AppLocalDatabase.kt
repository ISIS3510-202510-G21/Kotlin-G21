package com.isis3510.growhub.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
// Import the classes related to the data persistence model
import com.isis3510.growhub.local.data.CategoryDao
import com.isis3510.growhub.local.data.CategoryEntity
import com.isis3510.growhub.local.data.EventEntity
import com.isis3510.growhub.local.data.EventDao
import com.isis3510.growhub.utils.Converters


@Database(entities = [CategoryEntity::class, EventEntity::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppLocalDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppLocalDatabase? = null

        fun getDatabase(context: Context): AppLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppLocalDatabase::class.java,
                    "app_local_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}