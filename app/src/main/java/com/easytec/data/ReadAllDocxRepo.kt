package com.easytec.data

import android.app.Activity
import android.net.Uri
import com.easytec.models.ImageFolder
import com.easytec.models.TotalFilesModel
import com.easytec.models.Videos
import java.io.File

interface ReadAllDocxRepo {
    suspend fun getAllDocx(context:Activity):ArrayList<TotalFilesModel>
    suspend fun getDocumentFoldersWithFileDetails(directory: File): Map<File, List<TotalFilesModel>>
    suspend fun getAllImageFolders(context: Activity): ArrayList<ImageFolder>
    suspend fun getAllVideosFolders(context: Activity): ArrayList<Videos>
    suspend fun getImagesFromFolder(folderName: String,context: Activity): ArrayList<Uri>

}
