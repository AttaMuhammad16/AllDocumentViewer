package com.easytec.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Directory
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.easytec.R
import com.easytec.databinding.ActivityTextToPdfBinding
import com.easytec.utils.Utils
import com.easytec.utils.Utils.createPdf
import com.easytec.utils.Utils.refreshMediaScanner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class TextToPdfActivity : AppCompatActivity() {
    lateinit var binding:ActivityTextToPdfBinding
    var fileName=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this@TextToPdfActivity,R.layout.activity_text_to_pdf)
        Utils.statusBarColor(this)
        binding.textToPdf.requestFocus()

        binding.backArrowImg.setOnClickListener {
            Utils.navigationToMainActivity(this, binding.backArrowImg) {
                onBackPressed()
            }
        }

        binding.checkImg.setOnClickListener {
            var text=binding.textToPdf.text.toString()
            if (text.isNotEmpty()){
                showCustomDialog()
            }else{
                Toast.makeText(this@TextToPdfActivity, "Please Enter Text", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showCustomDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.filename_dialog, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

        val fileNameEdt: EditText = view.findViewById(R.id.fileNameEdt)
        val cancelBtn: Button = view.findViewById(R.id.cancel_button)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        val createBtn: Button =view.findViewById(R.id.add_button)
        createBtn.setOnClickListener {
            val text=binding.textToPdf.text.toString()
            if (fileNameEdt.text.isNotEmpty() && text.isNotEmpty()){
                val pdfDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                fileName = fileNameEdt.text.toString().trim().replace("\\s+".toRegex(), "").replace("[\\\\/:*?\"<>|]".toRegex(), "").lowercase()

                GlobalScope.launch(Dispatchers.IO) {
                    createPdf(text,fileName,pdfDirectory,this@TextToPdfActivity)
                }

                val path=File(pdfDirectory.absolutePath,"$fileName.pdf")
                refreshMediaScanner(this@TextToPdfActivity,path.toString())
                dialog.dismiss()
            }else{
                Toast.makeText(this@TextToPdfActivity, "Please Enter FileName", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }
}