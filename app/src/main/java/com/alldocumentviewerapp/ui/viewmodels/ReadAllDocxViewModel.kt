package com.alldocumentviewerapp.ui.viewmodels

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.alldocumentviewerapp.data.ReadAllDocxRepo
import com.alldocumentviewerapp.models.ImageFolder
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.models.Videos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReadAllDocxViewModel @Inject constructor(private val readAllDocxRepo: ReadAllDocxRepo):ViewModel() {
    suspend fun getAllDocx(context:Activity):ArrayList<TotalFilesModel>{
        return withContext(Dispatchers.IO){readAllDocxRepo.getAllDocx(context)}
    }

    suspend fun getDocumentFoldersWithFileDetails(directory: File): Map<File, List<TotalFilesModel>>{
        return withContext(Dispatchers.IO){readAllDocxRepo.getDocumentFoldersWithFileDetails(directory)}
    }
    suspend fun getAllImageFolders(context: Activity): ArrayList<ImageFolder>{
        return withContext(Dispatchers.IO){readAllDocxRepo.getAllImageFolders(context)}
    }

    suspend fun getImagesFromFolder(folderName: String,context: Activity): ArrayList<Uri>{
        return withContext(Dispatchers.IO){readAllDocxRepo.getImagesFromFolder(folderName,context)}
    }
    suspend fun getAllVideosFolders(context: Activity):ArrayList<Videos>{
        return withContext(Dispatchers.IO){readAllDocxRepo.getAllVideosFolders(context)}
    }

}
