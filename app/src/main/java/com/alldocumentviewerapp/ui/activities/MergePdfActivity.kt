package com.alldocumentviewerapp.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityMergePdfBinding
import com.alldocumentviewerapp.utils.Utils
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfReader
import java.io.File
import java.io.FileOutputStream

class MergePdfActivity : AppCompatActivity() {
    lateinit var binding:ActivityMergePdfBinding
    private val PICK_PDF_FILES_REQUEST_CODE = 1
    var pdfPath=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_merge_pdf)
        Utils.statusBarColor(this)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }
        binding.selectPdfFilesBtn.setOnClickListener {
            pickPdfFiles()
        }

        binding.searchImg.setOnClickListener {
            if (pdfPath.isNotEmpty()){
                val file = File(pdfPath)
                val fileUri: Uri = FileProvider.getUriForFile(this, "com.alldocumentviewerapp.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                startActivity(Intent.createChooser(shareIntent, "Share Merge PDF"))
            }else{
                Toast.makeText(this@MergePdfActivity, "Select Pdf Files.", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun pickPdfFiles() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICK_PDF_FILES_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val fileList = mutableListOf<File>()
            data?.let {
                it.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        fileList.add(uriToFile(uri, this))
                    }
                } ?: it.data?.let { uri ->
                    fileList.add(uriToFile(uri, this))
                }

                if (fileList.size < 2) {
                    Toast.makeText(this, "Select at least two files to merge.", Toast.LENGTH_SHORT).show()
                    return
                }

                val outputDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val outputFileName = "mergedfile${System.currentTimeMillis()}"
                mergePdfFiles(fileList, outputDirectory, outputFileName)
            }
        }
    }


    fun uriToFile(uri: Uri, context: Context): File {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val tempFile = File.createTempFile("merge", ".pdf", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            return tempFile
        } ?: throw IllegalArgumentException("Could not open URI: $uri")
    }


    fun mergePdfFiles(fileList: List<File>, outputDirectory: File, outputFileName: String) {
        try {
            if (!outputDirectory.exists()) outputDirectory.mkdirs()

            val outputFile = File(outputDirectory, "$outputFileName.pdf")
            val outputStream = FileOutputStream(outputFile)
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)

            fileList.forEach { inputFile ->
                PdfReader(inputFile).use { pdfReader ->
                    PdfDocument(pdfReader).use { importedDocument ->
                        for (pageNum in 1..importedDocument.numberOfPages) {
                            importedDocument.copyPagesTo(pageNum, pageNum, pdfDocument)
                        }
                    }
                }
            }

            pdfDocument.close()
            outputStream.close()
            Utils.refreshMediaScanner(this@MergePdfActivity, outputFile.absolutePath)
            Toast.makeText(this@MergePdfActivity, "File Saved: ${outputFile.absolutePath}", Toast.LENGTH_SHORT).show()
            pdfPath=outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}