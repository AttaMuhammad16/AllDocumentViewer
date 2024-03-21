package com.alldocumentviewerapp.data

import android.app.Activity
import android.net.Uri
import com.alldocumentviewerapp.models.ImageFolder
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.models.Videos
import java.io.File

interface ReadAllDocxRepo {
    suspend fun getAllDocx(context:Activity):ArrayList<TotalFilesModel>
    suspend fun getDocumentFoldersWithFileDetails(directory: File): Map<File, List<TotalFilesModel>>
    suspend fun getAllImageFolders(context: Activity): ArrayList<ImageFolder>
    suspend fun getAllVideosFolders(context: Activity): ArrayList<Videos>
    suspend fun getImagesFromFolder(folderName: String,context: Activity): ArrayList<Uri>

}
