package com.alldocumentviewerapp.data.notesrepo

import com.alldocumentviewerapp.EasyTechDatabase
import com.alldocumentviewerapp.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NotesAppRepoImpl @Inject constructor(private val easyTechDatabase: EasyTechDatabase):NotesAppRepo {

    override suspend fun getNotes(): Flow<List<Note>> = flow{
        easyTechDatabase.noteDao().get().collect { notes ->
                emit(notes)
        }
    }

    override suspend fun putNotes(note: Note) {
        easyTechDatabase.noteDao().insert(note)
    }

    override suspend fun updateNotes(note: Note) {
        easyTechDatabase.noteDao().update(note)
    }

    override suspend fun deleteNotes(note: Note) {
        easyTechDatabase.noteDao().delete(note)
    }

    override suspend fun getNoteById(id: Long): Note {
        var note=easyTechDatabase.noteDao().getById(id)
        return note
    }

}