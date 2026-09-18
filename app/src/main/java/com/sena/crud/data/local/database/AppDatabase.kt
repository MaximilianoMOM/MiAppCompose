package com.sena.crud.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sena.crud.data.local.dao.TaskDraftDao
import com.sena.crud.data.local.entity.TaskDraftEntity

@Database(entities = [TaskDraftEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val taskDraftDao: TaskDraftDao
}
