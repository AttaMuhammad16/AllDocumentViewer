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
import com.alldocumentviewerapp.adapters.SearchDocumentAdapter
import com.alldocumentviewerapp.databinding.ActivitySearchDocumentsBinding
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.ui.viewmodels.ReadAllDocxViewModel
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.showProgressDialog
import com.alldocumentviewerapp.utils.Utils.statusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SearchDocumentsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchDocumentsBinding
    val readAllDocxViewModel:ReadAllDocxViewModel by viewModels()
    lateinit var list:ArrayList<TotalFilesModel>
    var temp:ArrayList<TotalFilesModel> = ArrayList()
    lateinit var adapter:SearchDocumentAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_documents)
        statusBarColor(this)
        list= ArrayList()
        temp= ArrayList()


        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }


       Utils.searchViewTextClearSearchIconsColor(binding.searchView, this, R.color.white)
       Utils.setSearchViewHintColor(this, binding.searchView, R.color.hint_edt_color)
       binding.searchView.requestFocus()

        lifecycleScope.launch{
            val dialog=showProgressDialog(this@SearchDocumentsActivity,"Loading...")
            list=readAllDocxViewModel.getAllDocx(this@SearchDocumentsActivity)
            if (list.isEmpty()){
                binding.blank.visibility= View.VISIBLE
            }else{
                binding.blank.visibility= View.GONE
            }
            adapter= SearchDocumentAdapter(list,this@SearchDocumentsActivity)
            val layoutManager=LinearLayoutManager(this@SearchDocumentsActivity)
            binding.recyclerView.adapter=adapter
            binding.recyclerView.layoutManager=layoutManager
            adapter.notifyDataSetChanged()
            dialog.dismiss()

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
                adapter = SearchDocumentAdapter(temp, this@SearchDocumentsActivity)
                binding.recyclerView.adapter = adapter
                return true
            }
        })
    }
}