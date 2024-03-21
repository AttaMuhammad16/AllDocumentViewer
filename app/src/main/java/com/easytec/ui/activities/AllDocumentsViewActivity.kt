package com.easytec.ui.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.easytec.R
import com.easytec.databinding.ActivityAllDocumentsViewBinding
import com.easytec.models.TotalFilesModel
import com.easytec.ui.viewmodels.UploadFileViewModel
import com.easytec.utils.Utils
import com.easytec.utils.Utils.getFileExtension
import com.easytec.utils.Utils.isPdfPasswordProtected
import com.easytec.utils.Utils.statusBarColor
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.ReaderProperties
import com.itextpdf.text.exceptions.BadPasswordException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AllDocumentsViewActivity : AppCompatActivity() {
    lateinit var binding: ActivityAllDocumentsViewBinding
    var bundle: TotalFilesModel? = null
    var getExtension = ""
    val uploadFileViewModel :UploadFileViewModel by viewModels()
    var filePath=""
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_documents_view)
        statusBarColor(this@AllDocumentsViewActivity)

        bundle = intent.getParcelableExtra("document")
        binding.fileNameTv.text = bundle?.fileName
        filePath = bundle?.path?:"none"
        binding.fileNameTv.isSelected=true

        getExtension = getFileExtension(bundle!!.fileName)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        if (getExtension == ".pdf") {
            val bol=isPdfPasswordProtected(filePath?:null)

            if (bol){
                Toast.makeText(this@AllDocumentsViewActivity, "Password Protected file", Toast.LENGTH_SHORT).show()
                showCustomDialog()
            }else{
                binding.pdfView.visibility = View.VISIBLE
                binding.pdfView.fromFile(File(filePath)).defaultPage(0).enableSwipe(true).swipeHorizontal(false).enableDoubletap(true).onLoad { /* handle loading */ }.scrollHandle(null).pageFitPolicy(FitPolicy.WIDTH).load()
            }

        } else if (getExtension == ".xls") {
            binding.webView.visibility = View.VISIBLE
            readXlsFile(filePath)
        } else if (getExtension == ".xlsx") {
            binding.webView.visibility = View.VISIBLE
            readXlsxFile(filePath)
            uploadFiles(filePath)
        } else if (getExtension == ".doc" || getExtension==".docx"){
            uploadFiles(filePath)
        }else if (getExtension==".ppt"){
            uploadFiles(filePath)
        } else if(getExtension==".txt"){
            uploadFiles(filePath)
        } else if (getExtension==".rtf"){
            uploadFiles(filePath)
        } else {
            Toast.makeText(this@AllDocumentsViewActivity, "this file can not open", Toast.LENGTH_SHORT).show()
        }

        binding.moreImg.setOnClickListener {
            Utils.rippleEffect(this@AllDocumentsViewActivity, it)
            val popupMenu = PopupMenu(this@AllDocumentsViewActivity, it)
            popupMenu.menuInflater.inflate(R.menu.share_file_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.shareApp -> {
                            Utils.shareFileWithOthers(this@AllDocumentsViewActivity, File(filePath))
                            return true
                        }
                        R.id.delete -> {
                            val fileToDelete = File(filePath)
                            if (fileToDelete.exists()) {
                                if (fileToDelete.delete()) {
                                    Utils.refreshMediaScanner(this@AllDocumentsViewActivity,fileToDelete.path)
                                    finish()
                                    Toast.makeText(this@AllDocumentsViewActivity, "File deleted successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@AllDocumentsViewActivity, "Failed to delete file", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@AllDocumentsViewActivity, "File does not exist", Toast.LENGTH_SHORT).show()
                            }
                            return true
                        }
                        else -> return false
                    }
                }
            })
            popupMenu.show()
        }

    }

    private fun readXlsFile(filePath: String) {
        try {
            val file = File(filePath)
            val inputStream = FileInputStream(file)
            val workbook = HSSFWorkbook(inputStream)
            readExcelFile(workbook)

        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readXlsxFile(filePath: String) {
        try {
            val file = File(filePath)
            val inputStream = FileInputStream(file)

//            val factory = DocumentBuilderFactory.newInstance().apply {
//                isNamespaceAware = false
//            }
//            val builder = factory.newDocumentBuilder()
//            val document = builder.parse(inputStream)

            // Continue with the rest of your Xlsx processing logic
            val workbook = XSSFWorkbook(inputStream)
            Log.i("TAG", "readXlsxFile: $workbook")
            readExcelFile(workbook)
            workbook.close()
        } catch (e: Exception) {
            Log.e("TAG", "readXlsxFile: Error", e)
        }

    }

    private fun readExcelFile(workbook: Workbook) {
        try {
            val sheet = workbook.getSheetAt(0)
            val rows = sheet.iterator()
            val result = StringBuilder()
            result.append("<table style='border-collapse: collapse; width: 100%;'>")
            while (rows.hasNext()) {
                val currentRow = rows.next()
                val cellsInRow = currentRow.iterator()
                result.append("<tr style='border: 1px solid #000;'>")
                while (cellsInRow.hasNext()) {
                    val currentCell = cellsInRow.next()
                    result.append("<td style='border: 1px solid #000; padding: 8px;'>")
                    result.append(currentCell.toString())
                    result.append("</td>")
                }
                result.append("</tr>")
            }
            result.append("</table>")
            binding.webView.visibility = View.VISIBLE
            binding.webView.loadDataWithBaseURL(null, result.toString(), "text/html", "UTF-8", null)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            workbook.close()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun uploadFiles(filePath: String):String{
        binding.pd.visibility = View.VISIBLE
        binding.webView.apply {
            visibility = View.VISIBLE
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.displayZoomControls = true
            settings.supportZoom()
            settings.displayZoomControls=true
            settings.builtInZoomControls=true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
        }

        uploadFileViewModel.uploadFile(bundle!!,filePath,binding.pd,this@AllDocumentsViewActivity)

        binding.webView.webViewClient = object : WebViewClient() {

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Log.i("TAG", "Error loading URL: $error")
            }
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
        }
        var urlForReturn=""
        uploadFileViewModel.liveUrl.observe(this) { url ->
            urlForReturn=url
            binding.webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$url")
        }
        return urlForReturn
    }

    fun loadPdfIntoView(filePath: String, password: String?) {
        binding.pdfView.visibility = View.VISIBLE
        binding.pdfView.fromFile(File(filePath))
            .password(password)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onLoad { /* handle loading */ }
            .scrollHandle(null)
            .pageFitPolicy(FitPolicy.WIDTH)
            .load()
    }


    private fun showCustomDialog() {

        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.password_dialog, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

        val passwordEdt: EditText = view.findViewById(R.id.passwordEdt)
        val cancelBtn: Button = view.findViewById(R.id.cancel_button)
        val okBtn: Button =view.findViewById(R.id.okBtn)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        okBtn.setOnClickListener {
            var pass=passwordEdt.text.toString()

            if (pass.isNotEmpty()){
                try {
                    val readerProperties = ReaderProperties().setPassword(pass.toByteArray())
                    val reader = PdfReader(filePath, readerProperties)
                    PdfDocument(reader).use { pdfDocument ->
                        if (reader.isEncrypted) {
                            loadPdfIntoView(filePath, pass)
                        } else {
                            loadPdfIntoView(filePath, null)
                        }
                    }
                    dialog.dismiss()
                } catch (e: BadPasswordException) {
                    Toast.makeText(this@AllDocumentsViewActivity, "Wrong Password", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@AllDocumentsViewActivity, "Wrong Password", Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(this@AllDocumentsViewActivity, "Enter Password", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

        }
    }
}



