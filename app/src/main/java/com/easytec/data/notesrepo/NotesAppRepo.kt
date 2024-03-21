package com.easytec.data.notesrepo

import com.easytec.models.Note
import kotlinx.coroutines.flow.Flow

interface NotesAppRepo {
    suspend fun getNotes(): Flow<List<Note>>
    suspend fun putNotes(note: Note)
    suspend fun updateNotes(note: Note)
    suspend fun deleteNotes(note: Note)
    suspend fun getNoteById(id:Long):Note
}