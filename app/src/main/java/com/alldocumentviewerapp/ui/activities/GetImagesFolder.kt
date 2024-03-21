package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.GetImagesFolderAdapter
import com.alldocumentviewerapp.databinding.ActivityGetImagesFolderBinding
import com.alldocumentviewerapp.models.ImageFolder
import com.alldocumentviewerapp.ui.viewmodels.ReadAllDocxViewModel
import com.alldocumentviewerapp.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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