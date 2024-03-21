package com.easytec

import androidx.room.Database
import androidx.room.RoomDatabase
import com.easytec.models.Note
import com.easytec.models.NotesDAO


@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class EasyTechDatabase:RoomDatabase() {
    abstract fun noteDao() : NotesDAO
}

