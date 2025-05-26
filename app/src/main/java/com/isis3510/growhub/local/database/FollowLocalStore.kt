package com.isis3510.growhub.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.isis3510.growhub.local.data.FollowItemDao
import com.isis3510.growhub.local.data.FollowerEntity
import com.isis3510.growhub.local.data.FollowingEntity

@Database(
    entities = [FollowerEntity::class, FollowingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FollowLocalStore : RoomDatabase() {
    abstract fun followItemDao(): FollowItemDao

    companion object {
        @Volatile
        private var INSTANCE: FollowLocalStore? = null

        fun getDatabase(context: Context): FollowLocalStore {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FollowLocalStore::class.java,
                    "follow_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}