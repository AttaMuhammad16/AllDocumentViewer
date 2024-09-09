package com.alldocumentviewerapp.utils

import android.view.View
import android.widget.Toast
import com.alldocumentviewerapp.utils.Utils.isPdfPasswordProtected
import java.io.File

//        storageViewModel.liveUrl.observe(this) { fileUrl ->
//            if (fileUrl.isNotEmpty()) {
//                urlForReturn=fileUrl
//                val encodedUrl = URLEncoder.encode(fileUrl, "UTF-8")
//                var completeUrl=""
//                if (fileExtension==".ppt"){
//                    completeUrl="https://docs.google.com/gview?embedded=true&url=$encodedUrl"
//                }else if (fileExtension==".txt"){
//                    completeUrl="https://docs.google.com/gview?embedded=true&url=$encodedUrl"
//                } else{
//                    completeUrl = "https://view.officeapps.live.com/op/view.aspx?src=$encodedUrl"
//                }
//                binding.webView.loadUrl(completeUrl)
//            }
//        }


//if (fileExtension == ".pdf") {
//    val bol= isPdfPasswordProtected(filePath?:null)
//    if (bol){
//        Toast.makeText(this@AllDocumentsViewActivity, "Password Protected file", Toast.LENGTH_SHORT).show()
//        showCustomDialog()
//    }else{
//        binding.pdfView.visibility = View.VISIBLE
//        binding.pdfView.initWithFile(File(filePath))
//    }
//} else if (fileExtension == ".xls") {
//    binding.webView.visibility = View.VISIBLE
//    uploadFiles(filePath)
//} else if (fileExtension == ".xlsx") {
//    binding.webView.visibility = View.VISIBLE
//    uploadFiles(filePath)
//} else if (fileExtension == ".doc" || fileExtension==".docx"){
//    uploadFiles(filePath)
//}else if (fileExtension==".ppt"){
//    uploadFiles(filePath)
//} else if(fileExtension==".txt"){
//    uploadFiles(filePath)
//} else if (fileExtension==".rtf"){
//    uploadFiles(filePath)
//} else {
//    Toast.makeText(this@AllDocumentsViewActivity, "this file can not open", Toast.LENGTH_SHORT).show()
//}












//    @SuppressLint("SetJavaScriptEnabled")
//    fun uploadFiles(filePath: String) {
//        binding.pd.visibility = View.VISIBLE
//        binding.webView.apply {
//            visibility = View.VISIBLE
//            settings.javaScriptEnabled = true
//            settings.allowFileAccess = true
//            settings.allowContentAccess = true
//            settings.displayZoomControls = true
//            settings.supportZoom()
//            settings.displayZoomControls=true
//            settings.builtInZoomControls=true
//            settings.allowFileAccessFromFileURLs = true
//            settings.allowUniversalAccessFromFileURLs = true
//        }
//
//        storageViewModel.uploadFile(bundle!!,filePath,binding.pd,this@AllDocumentsViewActivity)
//
//        binding.webView.webViewClient = object : WebViewClient() {
//
//            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
//                Log.i("TAG", "Error loading URL: $error")
//            }
//            override fun onPageCommitVisible(view: WebView?, url: String?) {
//                super.onPageCommitVisible(view, url)
//            }
//            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                super.onPageStarted(view, url, favicon)
//            }
//        }
//    }





// pdf

//fun loadPdfIntoView(filePath: String, password: String?) {
//    binding.pdfView.visibility = View.VISIBLE
////        binding.pdfView.fromFile(File(filePath))
////            .password(password)
////            .defaultPage(0)
////            .enableSwipe(true)
////            .swipeHorizontal(false)
////            .enableDoubletap(true)
////            .onLoad { /* handle loading */ }
////            .scrollHandle(null)
////            .pageFitPolicy(FitPolicy.WIDTH)
////            .load()
//
//    binding.pdfView.initWithFile(File(filePath))
//}


