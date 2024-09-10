package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.SearchDocumentAdapter
import com.alldocumentviewerapp.databinding.ActivityZipUnZipBinding
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.utils.Utils
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ZipUnZipActivity : AppCompatActivity() {
    lateinit var binding: ActivityZipUnZipBinding
    lateinit var list:ArrayList<TotalFilesModel>
    var temp:ArrayList<TotalFilesModel> = ArrayList()
    lateinit var adapter: SearchDocumentAdapter
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_zip_un_zip)
        Utils.statusBarColor(this)
        list= ArrayList()
        list = intent.getParcelableArrayListExtra("zipFileList")!!

        if (list.isEmpty()){
            binding.blank.visibility= View.VISIBLE
        }else{
            binding.blank.visibility=View.GONE
        }

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        Utils.searchViewTextClearSearchIconsColor(binding.searchView, this, R.color.white)
        Utils.setSearchViewHintColor(this, binding.searchView, R.color.hint_edt_color)

        lifecycleScope.launch{
            joinAll()
            adapter= SearchDocumentAdapter(list,this@ZipUnZipActivity)
            var layoutManager= LinearLayoutManager(this@ZipUnZipActivity)
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
                adapter = SearchDocumentAdapter(temp, this@ZipUnZipActivity)
                binding.recyclerView.adapter = adapter
                return true
            }
        })


    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized){
            adapter.notifyDataSetChanged()
            Log.i("TAG", "called")
        }
    }
}