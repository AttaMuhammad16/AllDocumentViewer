package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityPrivacyPolicyBinding
import com.alldocumentviewerapp.utils.Utils.statusBarColor

class PrivacyPolicyActivity : AppCompatActivity() {
    lateinit var binding:ActivityPrivacyPolicyBinding
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_privacy_policy)
        statusBarColor(this)
        binding.webView.webViewClient= WebViewClient()
        binding.webView.settings.javaScriptEnabled=true
        binding.webView.loadUrl("https://sites.google.com/d/13TVxNW6Y0MxuWqCgF0OoWMjsP0Tk-5w1/p/1oyKJzGc7-wk12gKO1vClx7hnTYL_nL4R/edit")

    }
}