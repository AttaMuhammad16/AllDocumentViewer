package com.easytec.ui.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.easytec.R
import com.easytec.databinding.ActivityReadOnlinePdfBinding
import com.easytec.utils.Utils
import com.easytec.utils.Utils.isUrlValid
import com.easytec.utils.Utils.rippleEffect
import com.easytec.utils.Utils.statusBarColor

class ReadOnlinePdfActivity : AppCompatActivity() {
    lateinit var binding:ActivityReadOnlinePdfBinding
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_read_online_pdf)
        statusBarColor(this@ReadOnlinePdfActivity)
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


        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        binding.urlEdt.requestFocus()

        binding.searchImg.setOnClickListener {
            rippleEffect(this@ReadOnlinePdfActivity,it)
            var url=binding.urlEdt.text.toString().trim()
            if (url.isEmpty()){
                Toast.makeText(this@ReadOnlinePdfActivity, "Please enter Url", Toast.LENGTH_SHORT).show()
            }else if (!isUrlValid(url)){
                Toast.makeText(this@ReadOnlinePdfActivity, "Please enter correct Url", Toast.LENGTH_SHORT).show()
            }else if (!url.startsWith("http://")&&!url.startsWith("https://")){
                Toast.makeText(this@ReadOnlinePdfActivity, "Please enter correct Url", Toast.LENGTH_SHORT).show()
            } else{
                if (url.endsWith(".pdf")) {
                    url = "https://docs.google.com/gview?embedded=true&url=$url"
                }
                binding.webView.apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                }
            }
        }

    }

}