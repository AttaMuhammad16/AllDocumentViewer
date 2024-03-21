package com.easytec.ui.activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.util.rangeTo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.easytec.R
import com.easytec.databinding.ActivityPdfToImageBinding
import com.easytec.databinding.ActivityTextToPdfBinding
import com.easytec.utils.Utils
import com.easytec.utils.Utils.shareFileWithOthers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfToImageActivity : AppCompatActivity() {
    lateinit var binding:ActivityPdfToImageBinding
    private val REQUEST_CODE_SELECT_PDF = 1
    lateinit var filePath:File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this@PdfToImageActivity,R.layout.activity_pdf_to_image)
        Utils.statusBarColor(this)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        binding.selectorBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
            }
            startActivityForResult(intent, REQUEST_CODE_SELECT_PDF)
        }

    }

    fun convertPdfToBitmapsAndSave(context: Context, pdfUri: Uri, lifecycleScope: LifecycleCoroutineScope): List<Uri>? {
        val savedImageUris = mutableListOf<Uri>()
        var dialog= Utils.showProgressDialog(this,"Processing...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(pdfUri, "r")?.use { fileDescriptor ->
                    PdfRenderer(fileDescriptor).use { pdfRenderer ->
                        for (pageIndex in 0 until pdfRenderer.pageCount) {
                            val page = pdfRenderer.openPage(pageIndex)
                            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()
                            val savedUri = saveBitmapToDownloads(bitmap, pageIndex, context)
                            savedUri?.let { savedImageUris.add(it) }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Image Saved in Download Folder", Toast.LENGTH_LONG).show()
                    Utils.dismissProgressDialog(dialog)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                }
            }
        }
        return savedImageUris
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_PDF && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                convertPdfToBitmapsAndSave(this@PdfToImageActivity,uri,lifecycleScope)!!
            }
        }
    }

    fun saveBitmapToDownloads(bitmap: Bitmap, pageIndex: Int, context: Context): Uri? {
        val timestamp = System.currentTimeMillis()
        val filename = "pdf_to_images${pageIndex}_$timestamp.jpg"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API level 29) and above
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            context.contentResolver.run {
                val uri = insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
//                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)
                Log.i("TAG", "(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) $uri")

                if (uri != null) {
                    openOutputStream(uri)?.use { outputStream ->
                        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                            delete(uri, null, null)
                            null
                        } else {
                            uri
                        }
                    }
                } else null
            }
        } else {
            // Below Android 10 (API level 29)
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, filename)
            Log.i("TAG", "Below Android 10 (API level 29) $file")

            try {
                FileOutputStream(file).use { outputStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        null
                    } else {
                        Uri.fromFile(file)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

}