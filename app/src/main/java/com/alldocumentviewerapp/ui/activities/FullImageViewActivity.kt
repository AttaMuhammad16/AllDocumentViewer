package com.alldocumentviewerapp.ui.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.databinding.ActivityFullImageViewBinding
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.deleteFileFromUri
import com.alldocumentviewerapp.utils.Utils.getImageFilePath
import com.alldocumentviewerapp.utils.Utils.refreshMediaScanner
import com.alldocumentviewerapp.utils.Utils.rippleEffect
import com.alldocumentviewerapp.utils.Utils.shareFileWithOthersViaUri
import com.alldocumentviewerapp.utils.Utils.showProgressDialog
import kotlinx.coroutines.launch

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
            rippleEffect(this@FullImageViewActivity,it)
            shareFileWithOthersViaUri(this@FullImageViewActivity, uri)
        }

        binding.deleteImg.setOnClickListener {
            lifecycleScope.launch {
                rippleEffect(this@FullImageViewActivity,it)
                val progress= showProgressDialog(this@FullImageViewActivity,"deleting..")
                val path=getImageFilePath(this@FullImageViewActivity,uri)
                deleteFileFromUri(this@FullImageViewActivity,bundleUri)
                refreshMediaScanner(this@FullImageViewActivity,path)
                progress.dismiss()
                finish()
            }
        }

    }

}