package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.RecentFilesAdapter
import com.alldocumentviewerapp.databinding.ActivityRecentFilesBinding
import com.alldocumentviewerapp.models.CacheDirModel
import com.alldocumentviewerapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RecentFilesActivity : AppCompatActivity() {
    lateinit var binding: ActivityRecentFilesBinding
    lateinit var adapter:RecentFilesAdapter
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@RecentFilesActivity, R.layout.activity_recent_files)
        Utils.statusBarColor(this)

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        var list=ArrayList<List<CacheDirModel>>()
        binding.pd.visibility=View.VISIBLE
        var job = lifecycleScope.async((Dispatchers.IO)) {
            var data=readFilesFromCache(this@RecentFilesActivity)
            list.add(data)
        }
        lifecycleScope.launch{
            job.await()
            binding.pd.visibility=View.GONE
            list.forEach {
                var reverseList=it.reversed()
                adapter= RecentFilesAdapter(reverseList,this@RecentFilesActivity)
            }
            var layoutManager= LinearLayoutManager(this@RecentFilesActivity)
            binding.recyclerView.adapter=adapter
            binding.recyclerView.layoutManager=layoutManager
            adapter.notifyDataSetChanged()

        }
    }

    suspend fun readFilesFromCache(context: Context): List<CacheDirModel> {
        return try {
            val files = context.cacheDir.listFiles()
            val cacheDirModels = files?.mapNotNull { file ->
                if (file.isFile && file.name.endsWith(".json")) {
                    val json = file.readText()
                    CacheDirModel.fromJson(json)
                } else {
                    null
                }
            } ?: emptyList()
            cacheDirModels
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

}