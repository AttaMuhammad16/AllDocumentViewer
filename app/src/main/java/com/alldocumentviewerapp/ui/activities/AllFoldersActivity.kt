package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.AllFoldersAdapter
import com.alldocumentviewerapp.databinding.ActivityAllFoldersBinding
import com.alldocumentviewerapp.models.AllFolders
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.ui.viewmodels.ReadAllDocxViewModel
import com.alldocumentviewerapp.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class AllFoldersActivity : AppCompatActivity() {
    lateinit var adapter: AllFoldersAdapter
    lateinit var list: ArrayList<AllFolders>
    lateinit var binding: ActivityAllFoldersBinding
    val readAllDocxViewModel:ReadAllDocxViewModel by viewModels()
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_folders)
        list = ArrayList()
        Utils.statusBarColor(this)
        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        val job = CoroutineScope(Dispatchers.Default).async {
            withContext(Dispatchers.IO) {
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    val rootDirectory: File = Environment.getExternalStorageDirectory()
                    val documentFolders: Map<File, List<TotalFilesModel>> = readAllDocxViewModel.getDocumentFoldersWithFileDetails(rootDirectory)


                    for ((folder, fileDetailsList) in documentFolders) {
                        val allFolders = AllFolders(folder.name, fileDetailsList.size.toString(), ArrayList())
                        for (fileDetails in fileDetailsList) {
                            allFolders.fileModel.add(fileDetails)
                        }
                        list.add(allFolders)
                    }
                } else {
                    println("External storage is not mounted.")
                }
            }
        }

        lifecycleScope.launch {
            binding.pd.visibility= View.VISIBLE
            job.await()
            binding.pd.visibility= View.GONE
            adapter = AllFoldersAdapter(list, this@AllFoldersActivity)
            val layoutManager = LinearLayoutManager(this@AllFoldersActivity)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = layoutManager
            adapter.notifyDataSetChanged()
        }

    }

}