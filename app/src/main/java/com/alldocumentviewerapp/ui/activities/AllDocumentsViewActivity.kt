package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
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
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityAllDocumentsViewBinding
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.ui.viewmodels.StorageViewModel
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.getFileExtension
import com.alldocumentviewerapp.utils.Utils.isPdfPasswordProtected
import com.alldocumentviewerapp.utils.Utils.statusBarColor
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.ReaderProperties
import com.itextpdf.text.exceptions.BadPasswordException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder


@AndroidEntryPoint
class AllDocumentsViewActivity : AppCompatActivity() {
    lateinit var binding: ActivityAllDocumentsViewBinding
    var bundle: TotalFilesModel? = null
    var fileExtension = ""
    val storageViewModel :StorageViewModel by viewModels()
    var filePath=""
    var urlForReturn=""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_documents_view)
        statusBarColor(this@AllDocumentsViewActivity)

        bundle = intent.getParcelableExtra("document")
        binding.fileNameTv.text = bundle?.fileName
        filePath = bundle?.path?:"none"
        binding.fileNameTv.isSelected=true

        fileExtension = getFileExtension(bundle!!.fileName)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        storageViewModel.liveUrl.observe(this) { fileUrl ->
            if (fileUrl.isNotEmpty()) {
                urlForReturn=fileUrl
                val encodedUrl = URLEncoder.encode(fileUrl, "UTF-8")
                var completeUrl=""
                if (fileExtension==".ppt"){
                    completeUrl="https://docs.google.com/gview?embedded=true&url=$encodedUrl"
                }else if (fileExtension==".txt"){
                    completeUrl="https://docs.google.com/gview?embedded=true&url=$encodedUrl"
                } else{
                    completeUrl = "https://view.officeapps.live.com/op/view.aspx?src=$encodedUrl"
                }
                binding.webView.loadUrl(completeUrl)
            }
        }


        if (fileExtension == ".pdf") {
            val bol=isPdfPasswordProtected(filePath?:null)
            if (bol){
                Toast.makeText(this@AllDocumentsViewActivity, "Password Protected file", Toast.LENGTH_SHORT).show()
                showCustomDialog()
            }else{
                binding.pdfView.visibility = View.VISIBLE
                binding.pdfView.initWithFile(File(filePath))
            }
        } else if (fileExtension == ".xls") {
            binding.webView.visibility = View.VISIBLE
            uploadFiles(filePath)
        } else if (fileExtension == ".xlsx") {
            binding.webView.visibility = View.VISIBLE
            uploadFiles(filePath)
        } else if (fileExtension == ".doc" || fileExtension==".docx"){
            uploadFiles(filePath)
        }else if (fileExtension==".ppt"){
            uploadFiles(filePath)
        } else if(fileExtension==".txt"){
            uploadFiles(filePath)
        } else if (fileExtension==".rtf"){
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


    @SuppressLint("SetJavaScriptEnabled")
    fun uploadFiles(filePath: String) {
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

        storageViewModel.uploadFile(bundle!!,filePath,binding.pd,this@AllDocumentsViewActivity)

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
    }

    fun loadPdfIntoView(filePath: String, password: String?) {
        binding.pdfView.visibility = View.VISIBLE
//        binding.pdfView.fromFile(File(filePath))
//            .password(password)
//            .defaultPage(0)
//            .enableSwipe(true)
//            .swipeHorizontal(false)
//            .enableDoubletap(true)
//            .onLoad { /* handle loading */ }
//            .scrollHandle(null)
//            .pageFitPolicy(FitPolicy.WIDTH)
//            .load()

        binding.pdfView.initWithFile(File(filePath))
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
            val pass=passwordEdt.text.toString()

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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBackPressed() {
        super.onBackPressed()
        GlobalScope.launch {
            if (urlForReturn.isNotEmpty()){
                storageViewModel.deleteImageToFirebaseStorage(urlForReturn)
            }
        }
    }
}



