package com.easytec.ui.activities

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easytec.R
import com.easytec.adapters.GetImagesFolderAdapter
import com.easytec.adapters.SearchDocumentAdapter
import com.easytec.databinding.ActivityGetImagesFolderBinding
import com.easytec.models.ImageFolder
import com.easytec.models.TotalFilesModel
import com.easytec.ui.viewmodels.ReadAllDocxViewModel
import com.easytec.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
@AndroidEntryPoint
class GetImagesFolder : AppCompatActivity() {
    lateinit var binding: ActivityGetImagesFolderBinding
    lateinit var list:ArrayList<ImageFolder>
    lateinit var adapter: GetImagesFolderAdapter
    lateinit var temp:ArrayList<ImageFolder>
    val readAllDocxViewModel:ReadAllDocxViewModel by viewModels()
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_get_images_folder)
        Utils.statusBarColor(this)
        list= ArrayList()
        temp= ArrayList()

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        Utils.searchViewTextClearSearchIconsColor(binding.searchView, this, R.color.white)
        Utils.setSearchViewHintColor(this, binding.searchView, R.color.hint_edt_color)

        var job = CoroutineScope(Dispatchers.IO).async {
            list = readAllDocxViewModel.getAllImageFolders(this@GetImagesFolder)
        }

        lifecycleScope.launch{
            binding.pd.visibility=View.VISIBLE
            job.await()
            binding.pd.visibility=View.GONE
            adapter= GetImagesFolderAdapter(list,this@GetImagesFolder)
            var layoutManager= LinearLayoutManager(this@GetImagesFolder)
            binding.recyclerView.adapter=adapter
            binding.recyclerView.layoutManager=layoutManager
            adapter.notifyDataSetChanged()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                val lowercaseQuery = newText?.toLowerCase()
                temp.clear()
                for (data in list) {
                    val lowerFileName = data.folderName?.toLowerCase()
                    if (lowerFileName?.contains(lowercaseQuery.orEmpty()) == true) {
                        temp.add(data)
                    }
                }
                adapter = GetImagesFolderAdapter(temp, this@GetImagesFolder)
                binding.recyclerView.adapter = adapter
                return true
            }
        })
    }

}