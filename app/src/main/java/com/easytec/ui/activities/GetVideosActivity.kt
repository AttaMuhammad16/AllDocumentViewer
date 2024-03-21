package com.easytec.ui.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easytec.R
import com.easytec.adapters.GetVideosAdapter
import com.easytec.databinding.ActivityGetVideosBinding
import com.easytec.models.Videos
import com.easytec.ui.viewmodels.ReadAllDocxViewModel
import com.easytec.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GetVideosActivity : AppCompatActivity() {
    lateinit var binding: ActivityGetVideosBinding
    lateinit var list:ArrayList<Videos>
    lateinit var adapter: GetVideosAdapter
    lateinit var temp:ArrayList<Videos>
    val readAllDocxViewModel: ReadAllDocxViewModel by viewModels()
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_get_videos)
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
            list = readAllDocxViewModel.getAllVideosFolders(this@GetVideosActivity)
        }

        lifecycleScope.launch{
            binding.pd.visibility= View.VISIBLE
            job.await()
            binding.pd.visibility= View.GONE
            adapter= GetVideosAdapter(list,this@GetVideosActivity)
            var layoutManager= LinearLayoutManager(this@GetVideosActivity)
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
                    val lowerFileName = data.videoTitle?.toLowerCase()
                    if (lowerFileName?.contains(lowercaseQuery.orEmpty()) == true) {
                        temp.add(data)
                    }
                }
                adapter = GetVideosAdapter(temp, this@GetVideosActivity)
                binding.recyclerView.adapter = adapter
                return true
            }
        })
    }
}