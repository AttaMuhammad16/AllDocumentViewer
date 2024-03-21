package com.alldocumentviewerapp.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.alldocumentviewerapp.models.ImageFolder
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.models.Videos
import com.alldocumentviewerapp.utils.FileTypes
import com.alldocumentviewerapp.utils.Utils.getFileType
import com.alldocumentviewerapp.utils.Utils.isDocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.File
import javax.inject.Inject


class ReadAllDocxImpl @Inject constructor():ReadAllDocxRepo {
    var uniqueFolderNames: HashSet<String> = hashSetOf()

    override suspend fun getAllDocx(context: Activity): ArrayList<TotalFilesModel> {
        return loadDocument(context)
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun loadDocument(context: Activity):ArrayList<TotalFilesModel> {
        val cursor = getAllMediaFilesCursor(context)
        var list=ArrayList<TotalFilesModel>()
        var syn=CoroutineScope(Dispatchers.IO).async {
            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val pathCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dateCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeTypeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val path = it.getString(pathCol) ?: continue
                    val name = it.getString(nameCol) ?: continue
                    val dateTime = it.getLong(dateCol)
                    val type = it.getString(mimeTypeCol) ?: continue
                    val size = it.getLong(sizeCol)

                    val contentUri = ContentUris.appendId(MediaStore.Files.getContentUri("external").buildUpon(), id).build()
                    list.add(TotalFilesModel(contentUri,path,name,size,dateTime,type))
                }
            }
        }
        syn.await()
        return list
    }


    private fun getAllMediaFilesCursor(context:Activity): Cursor? {
        val projections = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATE_MODIFIED, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.SIZE)

        val sortBy = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val selectionArgs = FileTypes.values().map { it.mimeTypes }.flatten().filterNotNull().toTypedArray()

        val args = selectionArgs.joinToString { "?" }

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN ($args)"

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        return context.contentResolver.query(collection, projections, selection, selectionArgs, sortBy)
    }

    override suspend fun getDocumentFoldersWithFileDetails(directory: File): Map<File, List<TotalFilesModel>> {
        val documentFolders: MutableMap<File, List<TotalFilesModel>> = mutableMapOf()
        val files: Array<File>? = directory.listFiles()
        if (files != null) {
            val folderFileDetails = mutableListOf<TotalFilesModel>()

            for (file in files) {
                if (file.isDirectory) {
                    val subFolderMap = getDocumentFoldersWithFileDetails(file)
                    subFolderMap.forEach { (subFolder, subFileDetailsList) ->
                        documentFolders.merge(subFolder, subFileDetailsList) { currentList, newList ->
                            currentList + newList
                        }
                    }
                } else {
                    if (isDocumentFile(file)) {
                        folderFileDetails.add(TotalFilesModel(path = file.absolutePath, fileName = file.name, fileSize = file.length(), dateTime = file.lastModified(), type = getFileType(file),))
                    }
                }
            }
            if (folderFileDetails.isNotEmpty()) {
                documentFolders.merge(directory, folderFileDetails.toList()) { currentList, newList ->
                    currentList + newList
                }
            }
        }
        return documentFolders
    }

     override suspend fun getAllImageFolders(context: Activity): ArrayList<ImageFolder> {
        val imageFolders = ArrayList<ImageFolder>()
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media._ID)
        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)

        cursor?.use {
            val columnIndexFolderId = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val columnIndexFolderName = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val columnIndexImageId = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (it.moveToNext()) {
                val folderId = it.getString(columnIndexFolderId)
                val folderName = it.getString(columnIndexFolderName)
                val imageId = it.getLong(columnIndexImageId)

                if (uniqueFolderNames.add(folderName)) {
                    val folderCoverUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId)
                    var imageCount = 0
                    var job = CoroutineScope(Dispatchers.IO).async {
                        imageCount = getImageCount(folderId, context)
                    }
                    job.await()
                    val imageFolder = ImageFolder(folderName, folderCoverUri, imageCount)
                    imageFolders.add(imageFolder)
                }
            }
        }
        return imageFolders
    }

    private suspend fun getImageCount(folderId: String,context: Activity): Int {
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_ID)
        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, "${MediaStore.Images.Media.BUCKET_ID} = ?", arrayOf(folderId), null)
        cursor?.use {
            return it.count
        }
        return 0
    }

    override suspend fun getAllVideosFolders(context: Activity): ArrayList<Videos> {
        val videoFolders = ArrayList<Videos>()
        val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME)
        val cursor = context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)

        cursor?.use {
            val columnIndexVideoId = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val columnIndexVideoName = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            while (it.moveToNext()) {
                val videoId = it.getLong(columnIndexVideoId)
                val videoName = it.getString(columnIndexVideoName)

                val videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId)
                videoFolders.add(Videos(videoName, videoUri)) // You can set videoCount as 0 or omit it
            }
        }
        return videoFolders
    }

    override suspend fun getImagesFromFolder(folderName: String,context: Activity): ArrayList<Uri> {
        val images = ArrayList<Uri>()

        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN)
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(folderName)
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder)

        cursor?.use {
            val columnIndexId = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (it.moveToNext()) {
                val imageId = it.getLong(columnIndexId)
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId
                )
                images.add(imageUri)
            }
        }
        return images
    }

}