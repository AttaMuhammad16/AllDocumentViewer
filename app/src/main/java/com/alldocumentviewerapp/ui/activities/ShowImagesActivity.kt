package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.ShowImagesAdapter
import com.alldocumentviewerapp.databinding.ActivityShowImagesBinding
import com.alldocumentviewerapp.ui.viewmodels.ReadAllDocxViewModel
import com.alldocumentviewerapp.utils.Utils
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
    var bundle:String?=null
    lateinit var adapter: ShowImagesAdapter
    var previousList:ArrayList<Uri> = ArrayList()
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_images)
        Utils.statusBarColor(this)
        bundle=intent.getStringExtra("videoTitle")
        binding.titleTv.text=bundle
        listOfUri= ArrayList()
        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }
        lifecycleScope.launch {
            previousList=async (Dispatchers.IO){ readAllDocxViewModel.getImagesFromFolder(bundle!!,this@ShowImagesActivity) }.await()
            adapter= ShowImagesAdapter(previousList,this@ShowImagesActivity)
            val layoutManager= StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            binding.recyclerView.adapter=adapter
            binding.recyclerView.layoutManager=layoutManager
            adapter.notifyDataSetChanged()
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        val job=CoroutineScope(Dispatchers.IO).async {
            listOfUri=readAllDocxViewModel.getImagesFromFolder(bundle!!,this@ShowImagesActivity)
        }

        lifecycleScope.launch {
            job.await()
            if (listOfUri!=previousList){
                previousList=listOfUri
                adapter= ShowImagesAdapter(listOfUri,this@ShowImagesActivity)
                val layoutManager= StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
                binding.recyclerView.adapter=adapter
                binding.recyclerView.layoutManager=layoutManager
                adapter.notifyDataSetChanged()
            }
        }

    }
}