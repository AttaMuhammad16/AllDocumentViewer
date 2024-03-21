package com.alldocumentviewerapp.ui.activities

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityCreateZipFileBinding
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.dismissProgressDialog
import com.alldocumentviewerapp.utils.Utils.showProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.zeroturnaround.zip.ZipUtil
import java.io.File

class CreateZipFileActivity : AppCompatActivity() {
    lateinit var binding:ActivityCreateZipFileBinding
    var uris: ArrayList<Uri> = ArrayList()
    lateinit var progressDialog:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_create_zip_file)
        Utils.statusBarColor(this@CreateZipFileActivity)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        binding.selectPdfFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(Intent.createChooser(intent, "Select files"), 11)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11 && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            progressDialog=showProgressDialog(this@CreateZipFileActivity,"Progressing")
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    uris.add(uri)
                }
                var job=CoroutineScope(Dispatchers.IO).async {
                    createZipFile(uris,this@CreateZipFileActivity)
                }
                lifecycleScope.launch {
                    job.await()
                    uris.clear()
                }

            } else {
                data?.data?.let { uri ->
                    uris.add(uri)

                    var job=CoroutineScope(Dispatchers.IO).async {
                        createZipFile(uris,this@CreateZipFileActivity)
                    }
                    lifecycleScope.launch {
                        job.await()
                        uris.clear()
                    }
                }
            }
        }
    }


    suspend fun createZipFile(uris: List<Uri>, context: Context) {
        // Ensure the Documents directory exists
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        val zipFileName = "${documentsDir.absolutePath}/zipFile${System.currentTimeMillis()}.zip"
        var file=File(zipFileName)

        val tempFiles = mutableListOf<File>()
        try {
            uris.forEach { uri ->
               var job= CoroutineScope(Dispatchers.IO).async {
                    createTempFileFromUri(uri, context)
                }
                val tempFile = job.await()
                tempFiles.add(tempFile)
            }
            ZipUtil.packEntries(tempFiles.toTypedArray(), File(zipFileName))
            Utils.refreshMediaScanner(this@CreateZipFileActivity,zipFileName)
            withContext(Dispatchers.Main){

                Toast.makeText(this@CreateZipFileActivity, "$zipFileName", Toast.LENGTH_SHORT).show()
                binding.shareImg.visibility= View.VISIBLE
                dismissProgressDialog(progressDialog)

                binding.shareImg.setOnClickListener {
                    val fileUri: Uri = FileProvider.getUriForFile(this@CreateZipFileActivity, "com.alldocumentviewerapp.fileprovider", file)
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "application/*"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                    startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                }

            }
        } finally {
            tempFiles.forEach { it.delete() }
        }

    }

    private suspend fun createTempFileFromUri(uri: Uri, context: Context): File {
        var job=CoroutineScope(Dispatchers.IO).async {
            getFileName(uri, context) ?: "unknown"
        }
        val fileName =job.await()
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }
        }
        return tempFile
    }

    private fun getFileName(uri: Uri, context: Context): String? {
        var name: String? = null
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    name = cursor.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                name = name?.substring(cut + 1)
            }
        }
        return name
    }


}