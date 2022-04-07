package com.abdulaziz.newsapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NewsEntity::class, ChannelEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun newsDao(): NewsDao

    companion object {

        private var db: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "database_name")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
            return db!!
        }

    }

}