package com.alldocumentviewerapp.ui.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.UnZipAdapter
import com.alldocumentviewerapp.databinding.ActivityUnZipBinding
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.models.UnZipModel
import com.alldocumentviewerapp.utils.Utils
import com.alldocumentviewerapp.utils.Utils.showMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

class UnZipActivity : AppCompatActivity() {
    lateinit var binding: ActivityUnZipBinding
    var bundle: TotalFilesModel? = null
    lateinit var adapter:UnZipAdapter
    var list:ArrayList<UnZipModel> = ArrayList()
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_un_zip)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_un_zip)
        Utils.statusBarColor(this)
        bundle = intent.getParcelableExtra("document")
        binding.fileNameTv.text = bundle?.fileName
        val filePath = bundle?.path
        binding.fileNameTv.isSelected=true

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        val zipFile = File(filePath!!)
        val outputDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),packageName)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()

            lifecycleScope.launch{
                list=unzip(zipFile, outputDirectory)
                joinAll()
                adapter= UnZipAdapter(list,this@UnZipActivity)
                var layoutManager= LinearLayoutManager(this@UnZipActivity)
                binding.recyclerView.adapter=adapter
                binding.recyclerView.layoutManager=layoutManager
                adapter.notifyDataSetChanged()
            }

        }else{
            lifecycleScope.launch{
                var list=unzip(zipFile, outputDirectory)
                adapter= UnZipAdapter(list,this@UnZipActivity)
                var layoutManager= LinearLayoutManager(this@UnZipActivity)
                binding.recyclerView.adapter=adapter
                binding.recyclerView.layoutManager=layoutManager
                adapter.notifyDataSetChanged()
            }
        }

        binding.moreImg.setOnClickListener {
            showMenu(this@UnZipActivity,it,filePath)
        }
    }

    @Throws(IOException::class)
    suspend fun unzip(zipFile: File, outputDirectory: File):ArrayList<UnZipModel> {
        val resultItems = ArrayList<UnZipModel>()
        var dialog=Utils.showProgressDialog(this,"Processing...")
        var job=CoroutineScope(Dispatchers.IO).async {
            try {
                ZipInputStream(FileInputStream(zipFile)).use { zis ->
                    val buffer = ByteArray(1024)
                    var zipEntry = zis.nextEntry
                    while (zipEntry != null) {

                        val newFile = File(outputDirectory, zipEntry.name)
//                        val model = UnZipModel(name = newFile.name, path = newFile.path, isDirectory = zipEntry.isDirectory, fileSize = zipEntry.size)
//                        resultItems.add(model)

                        if (zipEntry.isDirectory) {
                            newFile.mkdirs()
                        } else {
                            newFile.parentFile?.mkdirs()
                            FileOutputStream(newFile).use { fos ->
                                var len: Int
                                while (zis.read(buffer).also { len = it } > 0) {
                                    fos.write(buffer, 0, len)
                                }
                            }
                            val model = UnZipModel(name = newFile.name, path = newFile.path, isDirectory = zipEntry.isDirectory, fileSize = zipEntry.size)
                            resultItems.add(model)
                        }
                        zipEntry = zis.nextEntry
                    }
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
        job.join()
        Utils.dismissProgressDialog(dialog)
        return resultItems
    }

}