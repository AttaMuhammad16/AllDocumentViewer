package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.SearchDocumentAdapter
import com.alldocumentviewerapp.databinding.ActivityAllFolderFilesViewBinding
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.utils.Utils
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class AllFolderFilesViewActivity : AppCompatActivity() {
    lateinit var binding: ActivityAllFolderFilesViewBinding
    lateinit var list:ArrayList<TotalFilesModel>
    lateinit var adapter: SearchDocumentAdapter
    lateinit var temp:ArrayList<TotalFilesModel>
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_folder_files_view)
        Utils.statusBarColor(this)
        list= ArrayList()
        temp= ArrayList()
        list=intent.getParcelableArrayListExtra("allFiles")!!

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        Utils.searchViewTextClearSearchIconsColor(binding.searchView, this, R.color.white)
        Utils.setSearchViewHintColor(this, binding.searchView, R.color.hint_edt_color)
        binding.searchView.requestFocus()

        lifecycleScope.launch{
            joinAll()
            adapter= SearchDocumentAdapter(list,this@AllFolderFilesViewActivity)
            var layoutManager= LinearLayoutManager(this@AllFolderFilesViewActivity)
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
                    val lowerFileName = data.fileName?.toLowerCase()
                    if (lowerFileName?.contains(lowercaseQuery.orEmpty()) == true) {
                        temp.add(data)
                    }
                }
                adapter = SearchDocumentAdapter(temp, this@AllFolderFilesViewActivity)
                binding.recyclerView.adapter = adapter
                return true
            }
        })

    }
}