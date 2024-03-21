package com.easytec.ui.activities

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.easytec.R
import com.easytec.adapters.SearchDocumentAdapter
import com.easytec.adapters.ShowImagesAdapter
import com.easytec.databinding.ActivityAllFolderFilesViewBinding
import com.easytec.databinding.ActivityGetImagesFolderBinding
import com.easytec.databinding.ActivityShowImagesBinding
import com.easytec.models.TotalFilesModel
import com.easytec.ui.viewmodels.ReadAllDocxViewModel
import com.easytec.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
@AndroidEntryPoint
class ShowImagesActivity : AppCompatActivity() {
    lateinit var binding: ActivityShowImagesBinding
    lateinit var listOfUri:ArrayList<Uri>
    val readAllDocxViewModel:ReadAllDocxViewModel by viewModels()
    lateinit var adapter: ShowImagesAdapter
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_images)
        Utils.statusBarColor(this)
        var bundle=intent.getStringExtra("videoTitle")
        binding.titleTv.text=bundle
        listOfUri= ArrayList()
        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        var job=CoroutineScope(Dispatchers.IO).async {
            listOfUri=readAllDocxViewModel.getImagesFromFolder(bundle!!,this@ShowImagesActivity)
        }

        lifecycleScope.launch {
            job.await()
            Log.i("TAG", "onCreate:${listOfUri.size}")
            adapter= ShowImagesAdapter(listOfUri,this@ShowImagesActivity)
            var layoutManager= StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            binding.recyclerView.adapter=adapter
            binding.recyclerView.layoutManager=layoutManager
            adapter.notifyDataSetChanged()
        }
    }
}