package com.alldocumentviewerapp.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment

import com.alldocumentviewerapp.R
import com.alldocumentviewerapp.adapters.UnZipAdapter
import com.alldocumentviewerapp.models.TotalFilesModel
import com.alldocumentviewerapp.models.UnZipModel
import com.alldocumentviewerapp.utils.Utils
import com.github.junrar.Junrar

import java.io.File

class UnRarActivity : AppCompatActivity() {
    lateinit var adapter: UnZipAdapter
    var list:ArrayList<UnZipModel> = ArrayList()
    var bundle: TotalFilesModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_un_rar)
        Utils.statusBarColor(this)
        bundle = intent.getParcelableExtra("document")
        val filePath = bundle?.path
        val rarFile = File(filePath!!)
        val outputDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),packageName)

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
            Junrar.extract(rarFile, outputDirectory)
        }else{
            Junrar.extract(rarFile, outputDirectory)
        }
    }
}
