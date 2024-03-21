package com.easytec.modules

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.easytec.EasyTechDatabase
import com.easytec.data.ReadAllDocxImpl
import com.easytec.data.ReadAllDocxRepo
import com.easytec.data.notesrepo.NotesAppRepo
import com.easytec.data.notesrepo.NotesAppRepoImpl
import com.easytec.models.NotesDAO
import com.easytec.modules.password.openHelperFactory
import com.easytec.utils.PermissionHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
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