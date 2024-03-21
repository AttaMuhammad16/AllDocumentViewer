package com.easytec.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easytec.R
import com.easytec.adapters.SearchDocumentAdapter
import com.easytec.databinding.ActivitySheetsViewBinding
import com.easytec.databinding.ActivitySlidesViewBinding
import com.easytec.models.TotalFilesModel
import com.easytec.utils.Utils
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class SheetsViewActivity : AppCompatActivity() {
    lateinit var binding: ActivitySheetsViewBinding
    lateinit var list:ArrayList<TotalFilesModel>
    var temp:ArrayList<TotalFilesModel> =ArrayList()
    lateinit var adapter: SearchDocumentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sheets_view)
        Utils.statusBarColor(this)
        list= ArrayList()

        list = intent.getParcelableArrayListExtra("sheetsFilesList")!!
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
            adapter= SearchDocumentAdapter(list,this@SheetsViewActivity)
            var layoutManager= LinearLayoutManager(this@SheetsViewActivity)
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
                adapter = SearchDocumentAdapter(temp, this@SheetsViewActivity)
                binding.recyclerView.adapter = adapter
                return true
            }
        })
    }
}