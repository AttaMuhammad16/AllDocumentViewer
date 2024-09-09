package com.alldocumentviewerapp.ui.activities.rtffiles
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintJob
import android.print.PrintManager
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.ahmadullahpk.alldocumentreader.util.RtfHtmlDataType
import com.ahmadullahpk.alldocumentreader.util.RtfParseException
import com.ahmadullahpk.alldocumentreader.util.RtfReader
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityShowRtffilesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ShowRTFFiles : AppCompatActivity() {

    private lateinit var binding: ActivityShowRtffilesBinding
    private var fileName: String? = null
    private var filePath: String? = null
    private var fromConverterApp = false
    private var isFromAppActivity = false
    private lateinit var printAdapter: PrintDocumentAdapter
    private lateinit var printJob: PrintJob
    private lateinit var webView: WebView
    private var back = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=ContextCompat.getColor(this@ShowRTFFiles,R.color.main_app_color)
        try {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding = ActivityShowRtffilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadRTFFile()
    }

    private fun setupUI() {
        binding.imgBack.setOnClickListener { finish()}
        binding.imgShare.setOnClickListener { shareFile() }
        binding.imgPrint.setOnClickListener { createWebPrintJob(binding.webView) }

        if (intent != null) {
            filePath = intent.getStringExtra("path")
            fileName = intent.getStringExtra("name")
            isFromAppActivity = intent.getBooleanExtra("fromAppActivity", false)
            fromConverterApp = intent.getBooleanExtra("fromConverterApp", false)
            binding.headerTitleText.apply {
                maxLines = 1
                text = fileName
            }
        }

        webView = binding.webView
        webView.webViewClient = CustomWebViewClient()
        webView.settings.apply {
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = true
        }
    }

    private fun loadRTFFile() {
        // Show progress bar while loading the RTF file
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val html = loadRTFContent()

            // Once the data is loaded, update the UI on the main thread
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                if (html != null) {
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                    back = true
                } else {
                    Toast.makeText(this@ShowRTFFiles, "Failed to load file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun loadRTFContent(): String? {
        return try {
            val file = File(filePath)
            val rtfReader = RtfReader()
            val rtfHtmlDataType = RtfHtmlDataType()
            rtfReader.parse(file)
            rtfHtmlDataType.format(rtfReader.root, true)
        } catch (e: RtfParseException) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun createWebPrintJob(webView: WebView) {
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        printAdapter = webView.createPrintDocumentAdapter("New_RTF_File.pdf")
        printJob = printManager.print(
            getString(R.string.app_name) + " Document",
            printAdapter,
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("id", "print", 200, 200))
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        )
    }

    inner class CustomWebViewClient : WebViewClient() {
        override fun onPageStarted(webView: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(webView, url, favicon)
        }

        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            return false
        }
    }

    private fun shareFile() {
        val intent = Intent(Intent.ACTION_SEND)
        val uri = Uri.parse(filePath)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, "Share File"))
    }


}
