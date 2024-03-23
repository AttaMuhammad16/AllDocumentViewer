package com.alldocumentviewerapp.ui.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityFullImageViewBinding
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.shareFileWithOthersViaUri

class FullImageViewActivity : AppCompatActivity() {
    lateinit var binding:ActivityFullImageViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_full_image_view)
        Utils.statusBarColor(this)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        val bundleUri=intent.getStringExtra("imgUri")
        val uri= Uri.parse(bundleUri)
        binding.photoView.setImageURI(uri)

        binding.shareImg.setOnClickListener {
            shareFileWithOthersViaUri(this@FullImageViewActivity, uri)
        }

    }

}