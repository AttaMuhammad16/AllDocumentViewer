package com.alldocumentviewerapp.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.usage.StorageStatsManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.Browser
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.data.StorageUsageCallBack
import com.alldocumentviewerapp.models.TotalFilesModel
import com.google.android.gms.vision.CameraSource
import com.itextpdf.io.font.FontConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    const val PICK_IMAGE_REQUEST = 1
    val REQUEST_CAMERA_PERMISSION = 201

    fun statusBarColor(context: Activity) {
        context.window.statusBarColor = ContextCompat.getColor(context, R.color.widgetsColor)
    }

    inline fun rippleEffect(context: Context, view: View) {
        val rippleDrawable = RippleDrawable(
            ColorStateList.valueOf(context.resources.getColor(R.color.rippleColor)),
            null,
            null
        )
        view.background = rippleDrawable
    }

    inline fun navigationToMainActivity(
        context: Context,
        backArrowImg: ImageView,
        onBackPressedAction: () -> Unit
    ) {
        val rippleDrawable = RippleDrawable(
            ColorStateList.valueOf(context.resources.getColor(R.color.rippleColor)),
            null,
            null
        )
        backArrowImg.background = rippleDrawable
        onBackPressedAction.invoke()
    }

    inline fun searchViewTextClearSearchIconsColor(
        searchView: SearchView,
        context: Context,
        color: Int
    ) {
        (searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText).setTextColor(
            ContextCompat.getColor(context, color)
        )
        (searchView.findViewById<View>(androidx.appcompat.R.id.search_mag_icon) as ImageView).setColorFilter(
            ContextCompat.getColor(context, color)
        )
        (searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView).setColorFilter(
            ContextCompat.getColor(context, color)
        )
    }

    inline fun setSearchViewHintColor(
        context: Context,
        searchView: SearchView,
        color: Int,
        inputText: String? = null
    ) {
        val queryHint = "Search"
        val hintColor = ContextCompat.getColor(context, color)
        val spannable = SpannableString(queryHint)
        spannable.setSpan(
            ForegroundColorSpan(hintColor),
            0,
            queryHint.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (!inputText.isNullOrBlank()) {
            searchView.setQuery(inputText, false)
        } else {
            searchView.queryHint = spannable
        }
    }

    fun formatDateString(dateTime: Long): String {
        val date = Date(dateTime * 1000)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun formatFileSize(bytes: Long): String {
        val kiloByte = 1024
        val megaByte = kiloByte * 1024
        val gigaByte = megaByte * 1024
        return when {
            bytes < kiloByte -> "$bytes B"
            bytes < megaByte -> String.format("%.2f KB", bytes.toFloat() / kiloByte)
            bytes < gigaByte -> String.format("%.2f MB", bytes.toFloat() / megaByte)
            else -> String.format("%.2f GB", bytes.toFloat() / gigaByte)
        }
    }


    fun getFileExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) fileName.substring(dotIndex) else ""
    }

    fun getFileIconResource(fileExtension: String): Int {
        return when (fileExtension) {
            ".pdf" -> R.drawable.pdf
            ".doc", ".docx" -> R.drawable.word
            ".xls", ".xlsx" -> R.drawable.sheet
            ".txt" -> R.drawable.text
            ".ppt" -> R.drawable.slide
            ".zip" -> R.drawable.zip
            ".rar" -> R.drawable.rar
            ".rtf" -> R.drawable.rtf
            else -> R.drawable.unknown_file
        }
    }

    fun showProgressDialog(context: Context, message: String): Dialog {
        val progressDialog = Dialog(context)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.setCancelable(false)

        val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = message
        progressDialog.setContentView(view)
        progressDialog.show()
        return progressDialog
    }

    fun dismissProgressDialog(progressDialog: Dialog) {
        progressDialog?.dismiss()
    }


    fun getRTFFiles(): ArrayList<TotalFilesModel> {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val documentDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        val rtfFiles = ArrayList<TotalFilesModel>()

        fun readRTFFile(file: File): String {
            return "RTF content not implemented"
        }

        fun readFilesInDirectory(directory: File) {
            val files = directory.listFiles { file ->
                file.isFile && file.extension.toLowerCase() == "rtf"
            }

            files?.forEach { file ->
                val content = readRTFFile(file)
                val totalFilesModel = TotalFilesModel(
                    path = file.path,
                    fileName = file.name,
                    fileSize = file.length(),
                    dateTime = file.lastModified(),
                    type = "rtf",
                    uri = content.toUri()
                )
                rtfFiles.add(totalFilesModel)
                Log.i("rtffiles", "readFilesInDirectory:${file.name}")
            }
        }

        readFilesInDirectory(downloadsDirectory)
        readFilesInDirectory(documentDirectory)

        return rtfFiles
    }

    fun isDocumentFile(file: File): Boolean {
        val documentFileExtensions =
            arrayOf("pdf", "doc", "docx", "txt", "ppt", "pptx", "xls", "xlsx", "zip", "rar", "rtf")
        return documentFileExtensions.any { file.name.endsWith(it, ignoreCase = true) }
    }

    fun getFileType(file: File): String {
        val extension = file.extension.toLowerCase(Locale.getDefault())
        return when (extension) {
            "pdf" -> "PDF"
            "doc", "docx" -> "Word"
            "txt" -> "Text"
            "ppt", "pptx" -> "PowerPoint"
            "xls", "xlsx" -> "Excel"
            "zip" -> "Zip"
            "rar" -> "RAR"
            "rtf" -> "RTF"
            else -> "Unknown"
        }
    }

    fun selectMedia(
        context: Activity,
        type: String = "image/*",
        intentAction: String = Intent.ACTION_PICK,
        uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        requestCode: Int
    ) {
        val galleryIntent = Intent(intentAction, uri)
        galleryIntent.type = type
        context.startActivityForResult(galleryIntent, requestCode)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun refreshMediaScanner(context: Context, filePath: String) {
        MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { _, _ -> }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(File(filePath))
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } else {
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())
                )
            )
        }
    }

    suspend fun createPdf(text: String, fileName: String, pdfDirectory: File, context: Activity) {
        if (!pdfDirectory.exists()) {
            pdfDirectory.mkdir()
        }
        val pdfFile = File(pdfDirectory, "$fileName.pdf")
        val pdfDocument = PdfDocument(PdfWriter(pdfFile))
        val document = Document(pdfDocument)

        val font = PdfFontFactory.createFont(FontConstants.HELVETICA)
        val fontSize = 18f
        val paragraph = Paragraph(text).setFont(font).setFontSize(fontSize)
        document.add(paragraph)
        document.close()
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Pdf Created", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("Range")
    fun getImageFilePath(context: Context, uri: Uri): String {
        var path: String? = null
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATA),
                null,
                null,
                null
            )
            cursor?.moveToFirst()
            path = cursor?.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        } finally {
            cursor?.close()
        }
        return path!!
    }


    fun isPdfPasswordProtected(filePath: String?): Boolean {
        try {
            PdfReader(filePath)?.use { reader ->
                PdfDocument(reader)
                return reader.isEncrypted
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: com.itextpdf.kernel.crypto.BadPasswordException) {
            return true
        }
        return false
    }

    fun isUrlValid(url: String): Boolean {
        return URLUtil.isValidUrl(url)
    }

    inline fun myToast(context: Context, data: String, duration: Int) {
        Toast.makeText(context, data, duration).show()
    }

    inline fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }

    inline fun myColors(): IntArray {
        var itemColors = intArrayOf(
            R.color.color1,
            R.color.color2,
            R.color.color4,
            R.color.color5,
            R.color.color6,
            R.color.color7,
            R.color.color8,
            R.color.color9,
            R.color.color10,
            R.color.color11,
            R.color.color12,
            R.color.color13,
            R.color.color14,
            R.color.color15,
            R.color.color16,
            R.color.color17,
        )
        return itemColors
    }


    inline fun copyContentText(data: String, context: Context) {
        val text = data
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }

    inline fun isValidURL(url: String): Boolean {
        return try {
            var uri = Uri.parse(url)
            uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https")
        } catch (e: Exception) {
            false
        }
    }

    inline fun shareText(context: Context, extraSubject: String, extraText: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, extraSubject)
        shareIntent.putExtra(Intent.EXTRA_TEXT, extraText)
        context.startActivity(Intent.createChooser(shareIntent, "Share link via"))
    }

    inline fun openInChrome(context: Context, url: String) {
        val chromeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        chromeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        chromeIntent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome")
        context.startActivity(chromeIntent)
    }

    inline fun startCameraSource(
        cameraSource: CameraSource,
        context: Context,
        surfaceView: SurfaceView
    ) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            cameraSource.start(surfaceView.holder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    inline fun handleCameraPermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted.invoke()
            } else {
                onPermissionDenied.invoke()
            }
        }
    }


    fun getTotalStorageSpaceInGB(): Double {
        val statFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
        return bytesToGigabytes(totalBytes)
    }

    fun getAvailableStorageSpaceInGB(): Double {
        val statFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        return bytesToGigabytes(availableBytes)
    }

    private fun bytesToGigabytes(bytes: Long): Double {
        return bytes.toDouble() / (1024 * 1024 * 1024)
    }

    fun calculateUsedStoragePercentage(context: Context, callback: StorageUsageCallBack) {
        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val uuid = StorageManager.UUID_DEFAULT
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val freeBytes = storageStatsManager.getFreeBytes(uuid)
                val totalBytes = storageStatsManager.getTotalBytes(uuid)

                val usedSpacePercentage =
                    ((1 - freeBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

                withContext(Dispatchers.Main) {
                    callback.onStorageCalculated(usedSpacePercentage)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun shareAppLink(context: Context){
        val appPackageName = "com.alldocumentviewerapp"
        val playStoreLink = "All Document Viewer From Quantum App Works \nhttps://play.google.com/store/apps/details?id=$appPackageName"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, playStoreLink)
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }


    fun feedBackIntent(context: Context){
        val appPackageName = "com.alldocumentviewerapp"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
            context.startActivity(intent)
        }
    }


    fun shareFileWithOthers(context: Context, file: File) {
        val fileUri = FileProvider.getUriForFile(context, "com.alldocumentviewerapp.fileprovider", file)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "application/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(shareIntent, "Share File")
        context.startActivity(chooserIntent)
    }

    fun shareFileWithOthersViaUri(context: Context, uri:Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(shareIntent, "Share File")
        context.startActivity(chooserIntent)
    }



    fun showMenu(context: Activity,view: View,filePath:String){
        rippleEffect(context, view)
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.share_file_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.shareApp -> {
                        shareFileWithOthers(context, File(filePath))
                        return true
                    }
                    R.id.delete -> {
                        val fileToDelete = File(filePath)
                        if (fileToDelete.exists()) {
                            if (fileToDelete.delete()) {
                                refreshMediaScanner(context,fileToDelete.path)
                                context.finish()
                                Toast.makeText(context, "File deleted successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                        }
                        return true
                    }
                    else -> return false
                }
            }
        })
        popupMenu.show()
    }





    inline fun <T, VB : ViewBinding> RecyclerView.setData(
        items: List<T>,
        crossinline bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
        crossinline bindHolder: (binding: VB, item: T, position: Int, holder: DataViewHolder<VB>) -> Unit,
    ) {
        val adapter = object : RecyclerView.Adapter<DataViewHolder<VB>>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder<VB> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = bindingInflater(layoutInflater, parent, false)
                return DataViewHolder(binding)
            }
            override fun onBindViewHolder(holder: DataViewHolder<VB>, position: Int) {
                bindHolder(holder.binding, items[position], position, holder)
            }

            override fun getItemCount(): Int {
                return items.size
            }
        }
        this.adapter = adapter
    }
    class DataViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)




    @SuppressLint("QueryPermissionsNeeded")
    fun openFileWithOtherApps(context: Context, filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/x-rar-compressed") // MIME type for .rar files
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission
        }
        val chooserIntent = Intent.createChooser(intent, "Choose an app to open this file")
        context.startActivity(chooserIntent) // Start the chooser intent
    }



    suspend fun deleteFileFromUri(context: Context, uriString: String?) {
        if (uriString.isNullOrEmpty()) {
            return
        }

        val uri = Uri.parse(uriString)
        val contentResolver: ContentResolver = context.contentResolver

        try {
            // For content URIs, use ContentResolver to delete
            val result = contentResolver.delete(uri, null, null)

            if (result != null && result > 0) {
                // Successfully deleted
                Toast.makeText(context, "File deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                // File not found or deletion failed
                Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Handle any exceptions
            Toast.makeText(context, "Error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }










}