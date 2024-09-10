package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.SearchDocumentAdapter
import com.alldocumentviewerapp.databinding.ActivityRarfilesViewBinding
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.utils.Utils
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class RARFilesViewActivity : AppCompatActivity() {
    lateinit var binding: ActivityRarfilesViewBinding
    lateinit var list:ArrayList<TotalFilesModel>
    var temp:ArrayList<TotalFilesModel> = ArrayList()
    lateinit var adapter: SearchDocumentAdapter
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rarfiles_view)
        Utils.statusBarColor(this)
        list= ArrayList()
        list = intent.getParcelableArrayListExtra("rarFiles")!!

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
            adapter= SearchDocumentAdapter(list,this@RARFilesViewActivity)
            var layoutManager= LinearLayoutManager(this@RARFilesViewActivity)
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
                adapter = SearchDocumentAdapter(temp, this@RARFilesViewActivity)
                binding.recyclerView.adapter = adapter
                return true
            }
        })
    }
}