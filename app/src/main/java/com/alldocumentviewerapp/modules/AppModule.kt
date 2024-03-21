package com.alldocumentviewerapp.modules

import android.content.Context
import androidx.room.Room
import com.alldocumentviewerapp.EasyTechDatabase
import com.alldocumentviewerapp.data.ReadAllDocxImpl
import com.alldocumentviewerapp.data.ReadAllDocxRepo
import com.alldocumentviewerapp.data.notesrepo.NotesAppRepo
import com.alldocumentviewerapp.data.notesrepo.NotesAppRepoImpl
import com.alldocumentviewerapp.models.NotesDAO
import com.alldocumentviewerapp.modules.password.openHelperFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun getAllDocx(readAllDocxImpl: ReadAllDocxImpl):ReadAllDocxRepo=readAllDocxImpl


    @Provides
    @Singleton
    fun getNotesAppRepo(easyTechDatabase: EasyTechDatabase):NotesAppRepo  = NotesAppRepoImpl(easyTechDatabase)

    @Provides
    @Singleton
    fun provideMustToolDatabase(@ApplicationContext context: Context): EasyTechDatabase {
        return Room.databaseBuilder(context, EasyTechDatabase::class.java, "easytech_database").openHelperFactory(openHelperFactory).build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: EasyTechDatabase): NotesDAO {
        return database.noteDao()
    }

}

object password { // lock on data base.
    val openHelperFactory = SupportFactory(SQLiteDatabase.getBytes("!@@##$%%QWEERRTT/.,><~_++".toCharArray()))
}