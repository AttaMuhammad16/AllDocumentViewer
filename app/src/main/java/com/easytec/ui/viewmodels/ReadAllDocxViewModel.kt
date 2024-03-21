package com.easytec.ui.viewmodels

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.easytec.data.ReadAllDocxRepo
import com.easytec.models.ImageFolder
import com.easytec.models.TotalFilesModel
import com.easytec.models.Videos
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReadAllDocxViewModel @Inject constructor(private val readAllDocxRepo: ReadAllDocxRepo):ViewModel() {
    suspend fun getAllDocx(context:Activity):ArrayList<TotalFilesModel>{
        return readAllDocxRepo.getAllDocx(context)
    }

    suspend fun getDocumentFoldersWithFileDetails(directory: File): Map<File, List<TotalFilesModel>>{
        return readAllDocxRepo.getDocumentFoldersWithFileDetails(directory)
    }
    suspend fun getAllImageFolders(context: Activity): ArrayList<ImageFolder>{
        return readAllDocxRepo.getAllImageFolders(context)
    }

    suspend fun getImagesFromFolder(folderName: String,context: Activity): ArrayList<Uri>{
        return readAllDocxRepo.getImagesFromFolder(folderName,context)
    }
    suspend fun getAllVideosFolders(context: Activity):ArrayList<Videos>{
        return readAllDocxRepo.getAllVideosFolders(context)
    }

}
