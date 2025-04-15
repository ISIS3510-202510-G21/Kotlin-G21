package com.isis3510.growhub.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.isis3510.growhub.local.data.CategoryDao
import com.isis3510.growhub.local.data.CategoryEntity

@Database(entities = [CategoryEntity::class], version = 1, exportSchema = false)
abstract class AppLocalDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile // Ensures visibility of changes across threads
        private var INSTANCE: AppLocalDatabase? = null

        fun getDatabase(context: Context): AppLocalDatabase {
            // Return existing instance or build a new one synchronized
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppLocalDatabase::class.java,
                    "growhub_database"
                )
                    // Add migrations here if schema changes in the future
                    .build()
                INSTANCE = instance
                instance // Return the newly created instance
            }
        }
    }
}