package com.alldocumentviewerapp

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alldocumentviewerapp.models.Note
import com.alldocumentviewerapp.models.NotesDAO


@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class EasyTechDatabase:RoomDatabase() {
    abstract fun noteDao() : NotesDAO
}

